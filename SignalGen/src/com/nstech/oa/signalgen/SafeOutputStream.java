package com.nstech.oa.signalgen;

import java.io.OutputStream;

public abstract class SafeOutputStream extends OutputStream {

	@Override
	public abstract void close();

	@Override
	public abstract void flush();

	@Override
	public abstract void write(byte[] buffer);

	@Override
	public abstract void write(byte[] buffer, int offset, int count);

	@Override
	public abstract void write(int oneByte);

}