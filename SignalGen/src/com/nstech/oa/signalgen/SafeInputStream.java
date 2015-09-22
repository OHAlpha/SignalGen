package com.nstech.oa.signalgen;

import java.io.InputStream;

public abstract class SafeInputStream extends InputStream {

	@Override
	public abstract int available();

	@Override
	public abstract void close();

	@Override
	public abstract void mark(int readlimit);

	@Override
	public abstract boolean markSupported();

	@Override
	public abstract int read();

	@Override
	public abstract int read(byte[] buffer);

	@Override
	public abstract int read(byte[] buffer, int byteOffset, int byteCount);

	@Override
	public abstract void reset();

	@Override
	public abstract long skip(long byteCount);

}