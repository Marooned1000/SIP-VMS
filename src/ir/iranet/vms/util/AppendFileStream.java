package ir.iranet.vms.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public 	class AppendFileStream extends OutputStream {
		   RandomAccessFile fd;
		   public AppendFileStream(String file) throws IOException {
		     fd = new RandomAccessFile(file,"rw");
		     fd.seek(fd.length());
		     }
		   public void close() throws IOException {
		     fd.close();
		     }
		   public void write(byte[] b) throws IOException {
		     fd.write(b);
		     }
		   public void write(byte[] b,int off,int len) throws IOException {
		     fd.write(b,off,len);
		     }
		   public void write(int b) throws IOException {
		     fd.write(b);
		     }
		   }


