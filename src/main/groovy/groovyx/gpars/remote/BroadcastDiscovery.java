// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.UUID;

/**
 * <b>Not used</b> Local network discovery service.
 */
@SuppressWarnings({"MagicNumber"})
public class BroadcastDiscovery {
    @SuppressWarnings({"StaticNonFinalField"})
    private static InetAddress group;
    private static final int PORT = 4239;
    private static final long MAGIC = 0x23982391L;
    private final UUID uid;
    private final InetSocketAddress address;
    private Thread sendThread;
    private Thread receiveThread;
    private volatile boolean stopped;
    private MulticastSocket socket;

    static {
        try {
            group = InetAddress.getByName("230.0.0.239");
        } catch (UnknownHostException ignored) {
            group = null;
        }
    }

    public BroadcastDiscovery(final UUID uid, final InetSocketAddress address) {
        this.uid = uid;
        this.address = address;
    }

    public void start() {
        try {
            socket = new MulticastSocket(PORT);
            socket.joinGroup(BroadcastDiscovery.group);

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final DataOutputStream stream = new DataOutputStream(out);

            stream.writeLong(MAGIC);
            stream.writeLong(uid.getMostSignificantBits());
            stream.writeLong(uid.getLeastSignificantBits());
            stream.writeInt(address.getPort());
            final byte[] addrBytes = address.getAddress().getAddress();
            stream.writeInt(addrBytes.length);
            stream.write(addrBytes);
            stream.close();

            final byte[] bytes = out.toByteArray();

            sendThread = new Thread() {
                @Override
                public void run() {
                    while (!stopped) {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException ignore) {
                        }

                        try {
                            final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, BroadcastDiscovery.group, PORT);
                            socket.send(packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            sendThread.start();

            receiveThread = new Thread() {
                @Override
                public void run() {
                    final byte[] buf = new byte[3 * 8 + 3 * 4];
                    final byte[] addrBuf4 = new byte[4];
                    final byte[] addrBuf6 = new byte[6];
                    while (!stopped) {
                        final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        try {
                            socket.receive(packet);
                            final DataInputStream in = new DataInputStream(new ByteArrayInputStream(buf));
                            if (in.readLong() == MAGIC) {
                                final UUID uuid = new UUID(in.readLong(), in.readLong());
                                final int port = in.readInt();
                                final int addrLen = in.readInt();
                                if (addrLen == 4) {
                                    in.read(addrBuf4);
                                    onDiscovery(uuid, new InetSocketAddress(InetAddress.getByAddress(addrBuf4), port));
                                } else {
                                    in.read(addrBuf6);
                                    onDiscovery(uuid, new InetSocketAddress(InetAddress.getByAddress(addrBuf6), port));
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            receiveThread.start();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void stop() {
        try {
            stopped = true;

            if (sendThread != null) {
                sendThread.join();
            }

            if (receiveThread != null) {
                receiveThread.join();
            }

            if (socket != null) {
                socket.close();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void onDiscovery(final UUID uuid, final SocketAddress address) {
    }
}
