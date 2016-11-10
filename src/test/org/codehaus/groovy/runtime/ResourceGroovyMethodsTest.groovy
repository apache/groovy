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
package org.codehaus.groovy.runtime

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import static org.junit.Assert.assertEquals

class ResourceGroovyMethodsTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder()

	@Test
	void test_Should_write_String_to_File_using_default_encoding() {
		File file = temporaryFolder.newFile()
		String text = 'Hello World'
		
		ResourceGroovyMethods.write(file, text)
		
		assert file.text == text
	}

	@Test
	void test_Should_write_String_to_File_using_specified_encoding() {
		File file = temporaryFolder.newFile()
		String text = "؁"
		String encoding = 'UTF-8'
		
		ResourceGroovyMethods.write(file, text, encoding)
		
		assert file.getText(encoding) == text
	}

	@Test
	void test_Should_write_String_and_bom_to_File_using_specified_encoding() {
		File file = temporaryFolder.newFile()
		String text = "؁"
		String encoding = 'UTF-16LE'

		ResourceGroovyMethods.write(file, text, encoding, true)

		assert file.getBytes() == [-1, -2, 1, 6] as byte[]
	}

	@Test
	void test_Should_append_ByteArray_to_File() {
		File file = temporaryFolder.newFile()
		file.write('Hello')
		byte[] bytes = ' World'.bytes
		
		ResourceGroovyMethods.append(file, bytes)
		
		assert file.text == 'Hello World'
	}

	@Test
	void test_Should_append_String_to_File_using_default_encoding() {
		File file = temporaryFolder.newFile()
		file.write('Hello')
		
		ResourceGroovyMethods.append(file, ' World')
		
		assert file.text == 'Hello World'
	}

	@Test
	void test_Should_append_text_supplied_by_Reader_to_File_using_default_encoding() {
		File file = temporaryFolder.newFile()
		file.write('Hello')
		Reader reader = new StringReader(' World')
		
		ResourceGroovyMethods.append(file, reader)
		
		assert file.text == 'Hello World'
	}

	@Test
	void test_Should_append_text_supplied_by_Writer_to_File_using_default_encoding() {
		File file = temporaryFolder.newFile()
		file.write('Hello')
		
		Writer writer = new StringWriter()
		writer.append(' World')
		
		ResourceGroovyMethods.append(file, writer)
		
		assert file.text == 'Hello World'
	}

	@Test
	void test_Should_append_String_to_File_using_specified_encoding() {
		File file = temporaryFolder.newFile()
		String encoding = 'UTF-8'
		file.write('؁', encoding)
		
		ResourceGroovyMethods.append(file, ' ؁', encoding)
		
		assert file.getText(encoding) == '؁ ؁'
	}

	@Test
	void test_Should_append_text_supplied_by_Reader_to_File_using_specified_encoding() {
		File file = temporaryFolder.newFile()
		String encoding = 'UTF-8'
		file.write('؁', encoding)
		Reader reader = new CharArrayReader([' ','؁'] as char[])
		
		ResourceGroovyMethods.append(file, reader, encoding)
		
		assert file.getText(encoding) == '؁ ؁'
	}

	@Test
	void test_Should_append_text_supplied_by_Writer_to_File_using_specified_encoding() {
		File file = temporaryFolder.newFile()
		String encoding = 'UTF-8'
		file.write('؁', encoding)
		
		Writer writer = new CharArrayWriter()
		writer.append(' ')
		writer.append('؁')
		
		ResourceGroovyMethods.append(file, writer, encoding)
		
		assert file.getText(encoding) == '؁ ؁'
	}

	@Test
	void test_Should_write_String_to_File_using_specified_encoding_with_bom() {
		File file = temporaryFolder.newFile()
		String text = "؁"
		String encoding = 'UTF-16LE'

		ResourceGroovyMethods.write(file, text, encoding, true)

		assert file.getBytes() == [-1, -2, 1, 6] as byte[]
	}

	@Test
	void test_Should_append_String_to_File_using_specified_encoding_with_bom() {
		File file = temporaryFolder.newFile()
		file.delete()
		String encoding = 'UTF-16LE'
		file.append('؁', encoding, true)

		ResourceGroovyMethods.append(file, ' ؁', encoding, true)

		assert file.getBytes() == [-1, -2, 1, 6, 32, 0, 1, 6] as byte[]
	}

	@Test
	void test_Should_append_text_supplied_by_Reader_to_File_using_specified_encoding_with_bom() {
		File file = temporaryFolder.newFile()
		file.delete()
		String encoding = 'UTF-16LE'
		file.append('؁', encoding, true)
		Reader reader = new CharArrayReader([' ','؁'] as char[])

		ResourceGroovyMethods.append(file, reader, encoding, true)

		assert file.getBytes() == [-1, -2, 1, 6, 32, 0, 1, 6] as byte[]
	}

	@Test
	void test_Should_append_text_supplied_by_Writer_to_File_using_specified_encoding_with_bom() {
		File file = temporaryFolder.newFile()
		file.delete()
		String encoding = 'UTF-16LE'
		file.append('؁', encoding, true)

		Writer writer = new CharArrayWriter()
		writer.append(' ')
		writer.append('؁')

		ResourceGroovyMethods.append(file, writer, encoding, true)

		assert file.getBytes() == [-1, -2, 1, 6, 32, 0, 1, 6] as byte[]
	}

	@Test
	void testFileDirectorySizeExceptions() {
		try {
			ResourceGroovyMethods.directorySize(new File("doesn't exist"))
			fail("directorySize() should fail when directory specified doesn't exist")
		} catch (IOException expected) {
		}

		File tempFile = File.createTempFile("testDirectorySizeExceptions", "")

		try {
			ResourceGroovyMethods.directorySize(tempFile)
			fail("directorySize() should fail when a file is specified")
		} catch (IllegalArgumentException expected) {
		}

		tempFile.delete()
	}

	@Test
	void testDirectorySize() {
		File tempFile = File.createTempFile("__testDirectorySize__", "")
		delete(tempFile)

		File testDir = new File(tempFile.getAbsolutePath())
		testDir.mkdirs()

		final int nFiles = 3
		final int maxFileSize = 102488

		long totalSize = 0
		Random r = new Random(new Random(System.currentTimeMillis()).nextLong())

		for (int j = 0; j < nFiles; j++) {
			int fileSize = r.nextInt(maxFileSize)
			String path = (r.nextBoolean() ? "a/" : "") + (r.nextBoolean() ? "b/" : "") +
					(r.nextBoolean() ? "c/" : "") + (r.nextBoolean() ? "d/" : "") +
					(r.nextBoolean() ? "e/" : "") + (r.nextBoolean() ? "f" : "")

			String filePath = String.format("%s/%s/%s", path, j, fileSize)
			createFile(new File(testDir, filePath), fileSize)
			totalSize += fileSize
		}

		assertEquals(totalSize, ResourceGroovyMethods.directorySize(testDir))
		delete(testDir)
	}

	@Test
    void testRelativePath() {
        assertEquals("b", ResourceGroovyMethods.relativePath(new File("a"), new File("a/b")))
        assertEquals("b/a", ResourceGroovyMethods.relativePath(new File("a"), new File("a/b/a")))
        assertEquals("../b", ResourceGroovyMethods.relativePath(new File("a"), new File("b")))
        assertEquals("../../b", ResourceGroovyMethods.relativePath(new File("a/b"), new File("b")))
        assertEquals("", ResourceGroovyMethods.relativePath(new File("a"), new File("a")))
    }

	/**
	 * Creates empty file of size specified.
	 *
	 * @param file file to create
	 * @param size file size
	 */
	private static void createFile(File file, int size) {
		file.getParentFile().mkdirs()

		try {
			OutputStream os = new FileOutputStream(file)
			os.write(new byte[size])
			os.close()
		} catch (IOException e) {
			throw new RuntimeException(String.format("Failed to create [%s] of size [%s]: %s",
			file.getAbsolutePath(), size, e),
			e)
		}

		if (file.length() != size) {
			throw new RuntimeException(String.format("Failed to create [%s] of size [%s]",
			file.getAbsolutePath(), size))
		}
	}

	/**
	 * Deletes file or directory specified. If directory is not empty, all its files are deleted as well.
	 *
	 * @param file file or directory to delete
	 */
	private static void delete(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				delete(f)
			}
		}

		if (!file.delete()) {
			// Sometimes empty directory is not deleted on the first attempt
			try {
				Thread.sleep(1000)
			} catch (InterruptedException ignored) {
			}

			if (!file.delete()) {
				throw new RuntimeException(String.format("Failed to delete [%s]", file.getAbsolutePath()))
			}
		}

		if (file.exists()) {
			throw new RuntimeException(String.format("[%s] is not deleted", file.getAbsolutePath()))
		}
	}
}
