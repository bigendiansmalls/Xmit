package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.List;

// ---------------------------------------------------------------------------------//
class DataBlock
//---------------------------------------------------------------------------------//
{
  private final int offset;
  private final Header header;
  private final List<BlockPointer> blockPointers = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  DataBlock (int offset, Header header)
  // ---------------------------------------------------------------------------------//
  {
    this.offset = offset;
    this.header = header;
  }

  // ---------------------------------------------------------------------------------//
  void add (BlockPointer blockPointer)
  // ---------------------------------------------------------------------------------//
  {
    blockPointers.add (blockPointer);
  }

  // ---------------------------------------------------------------------------------//
  Header getHeader ()
  // ---------------------------------------------------------------------------------//
  {
    return header;
  }

  // ---------------------------------------------------------------------------------//
  int getSize ()
  // ---------------------------------------------------------------------------------//
  {
    return header.getSize ();
  }

  // ---------------------------------------------------------------------------------//
  public int totalBlockPointers ()
  // ---------------------------------------------------------------------------------//
  {
    return blockPointers.size ();
  }

  // ---------------------------------------------------------------------------------//
  byte getType ()
  // ---------------------------------------------------------------------------------//
  {
    return header.buffer[0];
  }

  // ---------------------------------------------------------------------------------//
  boolean ttlMatches (byte[] ttl)
  // ---------------------------------------------------------------------------------//
  {
    return header.ttlMatches (ttl);
  }

  // ---------------------------------------------------------------------------------//
  long getTtl ()
  // ---------------------------------------------------------------------------------//
  {
    return header.getTtl ();
  }

  // ---------------------------------------------------------------------------------//
  int getTwoBytes ()
  // ---------------------------------------------------------------------------------//
  {
    if (header.getSize () == 0)
      return 0;

    BlockPointer blockPointer = blockPointers.get (0);
    return Utility.getTwoBytes (blockPointer.buffer, blockPointer.offset);
  }

  // ---------------------------------------------------------------------------------//
  byte[] getEightBytes ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] eightBytes = new byte[8];
    if (header.getSize () == 0)
      return eightBytes;

    BlockPointer blockPointer = blockPointers.get (0);
    System.arraycopy (blockPointer.buffer, blockPointer.offset, eightBytes, 0,
        eightBytes.length);

    return eightBytes;
  }

  // ---------------------------------------------------------------------------------//
  byte[] getBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = new byte[getSize ()];
    int ptr = 0;
    for (BlockPointer blockPointer : blockPointers)
    {
      System.arraycopy (blockPointer.buffer, blockPointer.offset, buffer, ptr,
          blockPointer.length);
      ptr += blockPointer.length;
    }
    assert ptr == buffer.length;
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  int packBuffer (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    assert buffer.length >= offset + getSize ();

    for (BlockPointer blockPointer : blockPointers)
    {
      System.arraycopy (blockPointer.buffer, blockPointer.offset, buffer, offset,
          blockPointer.length);
      offset += blockPointer.length;
    }

    return offset;
  }

  // ---------------------------------------------------------------------------------//
  boolean isXmit ()
  // ---------------------------------------------------------------------------------//
  {
    if (header.getSize () == 0)       // see FILE392.XMI/$NULL
      return false;

    BlockPointer blockPointer = blockPointers.get (0);
    return Utility.matches (Reader.INMR01, blockPointer.buffer, blockPointer.offset + 1);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%08X  %s   %,7d  %,6d", offset, header, getSize (),
        blockPointers.size ());
  }
}
