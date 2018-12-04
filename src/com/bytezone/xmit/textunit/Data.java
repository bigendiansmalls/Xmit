package com.bytezone.xmit.textunit;

import com.bytezone.common.Utility;
import com.bytezone.xmit.Reader;

class Data
{
  int length;
  byte[] data;
  String text;
  boolean printable = true;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  Data (byte[] buffer, int ptr)
  {
    length = Reader.getWord (buffer, ptr);
    data = new byte[length];
    System.arraycopy (buffer, ptr + 2, data, 0, length);

    for (byte b : data)
      if ((b & 0xFF) <= 0x3F)
      {
        printable = false;
        break;
      }
    text = printable ? Reader.getString (data, 0, length) : "";
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%04X %s : %s", length, Utility.getHex (data, 0, data.length),
        text);
  }
}