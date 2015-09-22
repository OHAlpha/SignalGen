package com.nstech.oa.signalgen;

import java.util.concurrent.Semaphore;

class BlockingBuffer {

	private final byte[] buffer;

	private int readIndex = 0;

	private int writeIndex = 0;

	private final Semaphore available;

	private final Semaphore free;

	private final Semaphore mutex;

	private int stepSize;

	private final SafeInputStream iStream;

	private final SafeOutputStream oStream;

	public BlockingBuffer() {
		this(4096, 1);
	}

	public BlockingBuffer(int capacity) {
		this(capacity, 1);
	}

	public BlockingBuffer(int capacity, int stepSize) {
		buffer = new byte[capacity];
		this.stepSize = stepSize < 1 ? 0 : stepSize;
		available = new Semaphore(0);
		free = new Semaphore(capacity);
		mutex = new Semaphore(1);
		iStream = new SafeInputStream() {

			@Override
			public int read() {
				int out = -1;
				try {
					available.acquire();
					mutex.acquire();
					out = buffer[readIndex++];
					readIndex %= buffer.length;
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException("Thread interrupted", e);
				} finally {
					mutex.release();
					free.release();
				}
				return out;
			}

			@Override
			public int available() {
				return available.availablePermits();
			}

			@Override
			public void close() {
			}

			@Override
			public void mark(int readlimit) {
				throw new RuntimeException("mark not supported");
			}

			@Override
			public boolean markSupported() {
				return false;
			}

			@Override
			public int read(byte[] buffer) {
				int out = 0;
				// while (out < buffer.length) {
				while (out == 0) {
					int step = BlockingBuffer.this.stepSize == 0 ? buffer.length
							: BlockingBuffer.this.stepSize;
					if (out + step > buffer.length)
						step = buffer.length - out;
					if (readIndex + step > BlockingBuffer.this.buffer.length)
						step = BlockingBuffer.this.buffer.length - readIndex;
					try {
						available.acquire(step);
						mutex.acquire();
						System.arraycopy(BlockingBuffer.this.buffer, readIndex,
								buffer, out, step);
						readIndex += step;
					} catch (InterruptedException e) {
						e.printStackTrace();
						if (out > 0)
							return out;
						else
							throw new RuntimeException("Thread interrupted", e);
					} finally {
						mutex.release();
						free.release(step);
					}
				}
				return out;
			}

			@Override
			public int read(byte[] buffer, int byteOffset, int byteCount) {
				int out = 0;
				// while (out < byteCount) {
				while (out == 0) {
					int step = BlockingBuffer.this.stepSize == 0 ? byteCount
							: BlockingBuffer.this.stepSize;
					if (out + step > byteCount)
						step = byteCount - out;
					if (readIndex + step > BlockingBuffer.this.buffer.length)
						step = BlockingBuffer.this.buffer.length - readIndex;
					try {
						available.acquire(step);
						mutex.acquire();
						System.arraycopy(BlockingBuffer.this.buffer, readIndex,
								buffer, byteOffset + out, step);
						readIndex += step;
					} catch (InterruptedException e) {
						e.printStackTrace();
						if (out > 0)
							return out;
						else
							throw new RuntimeException("Thread interrupted", e);
					} finally {
						mutex.release();
						free.release(step);
					}
				}
				return out;
			}

			@Override
			public synchronized void reset() {
			}

			@Override
			public long skip(long byteCount) {
				long out = 0;
				try {
					while (out < buffer.length) {
						long step = BlockingBuffer.this.stepSize == 0 ? byteCount
								: BlockingBuffer.this.stepSize;
						if (out + step > byteCount)
							step = byteCount - out;
						mutex.acquire();
						try {
							available.acquire((int) step);
							readIndex += step;
							free.release((int) step);
						} catch (InterruptedException e) {
							e.printStackTrace();
							if (out > 0)
								return out;
							else
								throw new RuntimeException(
										"Thread interrupted", e);
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					if (out > 0)
						return out;
					else
						throw new RuntimeException("Thread interrupted", e);
				} finally {
					mutex.release();
				}
				return out;
			}

		};
		oStream = new SafeOutputStream() {

			@Override
			public void close() {
			}

			@Override
			public void flush() {
			}

			@Override
			public void write(byte[] buffer) {
				int out = 0;
				// while (out < buffer.length) {
				while (out == 0) {
					int step = BlockingBuffer.this.stepSize == 0 ? buffer.length
							: BlockingBuffer.this.stepSize;
					if (out + step > buffer.length)
						step = buffer.length - out;
					if (writeIndex + step > BlockingBuffer.this.buffer.length)
						step = BlockingBuffer.this.buffer.length - writeIndex;
					try {
						free.acquire(step);
						mutex.acquire();
						System.arraycopy(buffer, out,
								BlockingBuffer.this.buffer, writeIndex, step);
						writeIndex += step;
					} catch (InterruptedException e) {
						e.printStackTrace();
						throw new RuntimeException("Thread interrupted", e);
					} finally {
						mutex.release();
						available.release(step);
					}
				}
			}

			@Override
			public void write(byte[] buffer, int offset, int count) {
				int out = 0;
				// while (out < count) {
				while (out == 0) {
					int step = BlockingBuffer.this.stepSize == 0 ? count
							: BlockingBuffer.this.stepSize;
					if (out + step > count)
						step = count - out;
					if (writeIndex + step > BlockingBuffer.this.buffer.length)
						step = BlockingBuffer.this.buffer.length - writeIndex;
					try {
						free.acquire(step);
						mutex.acquire();
						System.arraycopy(buffer, offset + out,
								BlockingBuffer.this.buffer, writeIndex, step);
						writeIndex += step;
					} catch (InterruptedException e) {
						e.printStackTrace();
						throw new RuntimeException("Thread interrupted", e);
					} finally {
						mutex.release();
						available.release(step);
					}
				}
			}

			@Override
			public void write(int oneByte) {
				try {
					free.acquire();
					mutex.acquire();
					buffer[writeIndex++] = (byte) oneByte;
					writeIndex %= buffer.length;
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException("Thread interrupted", e);
				} finally {
					mutex.release();
					available.release();
				}
			}

		};
	}

	public SafeInputStream getiStream() {
		return iStream;
	}

	public SafeOutputStream getoStream() {
		return oStream;
	}

	public int getCapacity() {
		return buffer.length;
	}

	public int getStepSize() {
		return stepSize;
	}

	public void setStepSize(int stepSize) {
		this.stepSize = stepSize < 1 ? 0 : stepSize;
	}

}