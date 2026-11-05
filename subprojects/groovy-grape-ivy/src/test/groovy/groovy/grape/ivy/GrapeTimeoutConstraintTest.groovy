/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.grape.ivy

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.apache.ivy.core.settings.IvySettings
import org.apache.ivy.core.settings.TimeoutConstraint
import org.apache.ivy.plugins.resolver.AbstractResolver
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * A stalled download fails after the timeouts of the {@code grape} timeout
 * constraint instead of blocking a grab forever.
 */
final class GrapeTimeoutConstraintTest {

    @TempDir
    File tmp

    private HttpServer server
    private final CountDownLatch release = new CountDownLatch(1)
    private final Map<String, String> savedProperties = [:]

    @BeforeEach
    void startServer() {
        server = HttpServer.create(new InetSocketAddress(InetAddress.loopbackAddress, 0), 0)
        server.createContext('/') { HttpExchange exchange ->
            try {
                String path = exchange.requestURI.path
                if (path.endsWith('/stall-1.0.pom')) {
                    byte[] pom = '''<project xmlns="http://maven.apache.org/POM/4.0.0">
                        <modelVersion>4.0.0</modelVersion>
                        <groupId>repro</groupId><artifactId>stall</artifactId><version>1.0</version>
                    </project>'''.bytes
                    exchange.sendResponseHeaders(200, pom.length)
                    exchange.responseBody.write(pom)
                } else if (path.endsWith('/stall-1.0.jar')) {
                    // send half of the content, then stall until the test ends
                    exchange.sendResponseHeaders(200, 2048)
                    exchange.responseBody.write(new byte[1024])
                    exchange.responseBody.flush()
                    release.await(2, TimeUnit.MINUTES)
                } else {
                    exchange.sendResponseHeaders(404, -1)
                }
            } finally {
                exchange.close()
            }
        }
        server.executor = null
        server.start()
    }

    @AfterEach
    void stopServer() {
        release.countDown()
        server.stop(0)
        savedProperties.each { name, value ->
            if (value == null) System.clearProperty(name) else System.setProperty(name, value)
        }
    }

    private String getRepositoryUrl() {
        "http://${server.address.hostString}:${server.address.port}/"
    }

    private void setProperty(String name, String value) {
        if (!savedProperties.containsKey(name)) savedProperties[name] = System.getProperty(name)
        System.setProperty(name, value)
    }

    private GrapeIvy newEngine(String resolvers) {
        def config = new File(tmp, 'grapeConfig.xml')
        config.text = """<ivysettings>
          <settings defaultResolver="downloadGrapes"/>
          <caches lockStrategy="artifact-lock-nio"/>
          <timeout-constraints>
            <timeout-constraint name="grape" connectionTimeout="1000" readTimeout="1000"/>
          </timeout-constraints>
          <resolvers>
            <chain name="downloadGrapes" returnFirst="true">
              <filesystem name="cachedGrapes">
                <ivy pattern="${tmp.absolutePath}/cache/[organisation]/[module]/ivy-[revision].xml"/>
                <artifact pattern="${tmp.absolutePath}/cache/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]"/>
              </filesystem>
              $resolvers
            </chain>
          </resolvers>
        </ivysettings>"""
        setProperty('grape.config', config.absolutePath)
        setProperty('grape.root', new File(tmp, 'grapes').absolutePath)
        new GrapeIvy()
    }

    /** Grabs the stalling artifact and returns what the grab threw, failing if it is still running. */
    private static Throwable grabStalled(GrapeIvy engine) {
        Throwable thrown = null
        def grab = Thread.startDaemon {
            try {
                engine.grab(autoDownload: true, group: 'repro', module: 'stall', version: '1.0', classLoader: new GroovyClassLoader())
            } catch (Throwable t) {
                thrown = t
            }
        }
        grab.join(30_000)
        assert !grab.alive : 'the grab of a stalled download is still running'
        thrown
    }

    @Test
    void shippedConfigsDefineTimeoutsForTheRemoteRepository() {
        for (name in ['default', 'relaxed']) {
            def settings = new IvySettings()
            settings.setVariable('user.home.url', tmp.toURI().toString())
            settings.load(GrapeIvy.resolveExplicitConfig(name))
            settings.validate()

            TimeoutConstraint grape = settings.getTimeoutConstraint('grape')
            assert grape != null : name
            assert grape.connectionTimeout > 0 && grape.readTimeout > 0 : name
            assert ((AbstractResolver) settings.getResolver('ibiblio')).timeoutConstraint.is(grape) : name
        }
    }

    @Test
    void stalledDownloadFromConfiguredResolverFails() {
        def engine = newEngine("""<ibiblio name="stall" root="$repositoryUrl" m2compatible="true" timeoutConstraint="grape"/>""")
        Throwable thrown = grabStalled(engine)
        assert thrown?.message?.contains('download failed: repro#stall;1.0!stall.jar')
    }

    @Test
    void stalledDownloadFromAddedResolverFails() {
        def engine = newEngine('')
        engine.addResolver(name: 'stall', root: repositoryUrl)
        Throwable thrown = grabStalled(engine)
        assert thrown?.message?.contains('download failed: repro#stall;1.0!stall.jar')
    }
}
