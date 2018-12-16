package com.bytezone.xmit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CatalogEntry implements Comparable<CatalogEntry>
{
  private static String line = "====== ---------+---------+---------+---------+"
      + "---------+---------+---------+---------+";

  private final String memberName;
  private String userName = "";
  private String aliasName = "";

  private int size;
  private int init;
  private int mod;
  private int vv;
  private int mm;
  private LocalDate date1;
  private LocalDate date2;
  private String time;

  final int blockFrom;
  //  int blockTo;

  private final List<String> lines = new ArrayList<> ();
  private final byte[] directoryData;
  private final List<BlockPointerList> blockPointerLists = new ArrayList<> ();

  private int bufferLength;
  private int dataLength;

  private final LogicalBuffer logicalBuffer = new LogicalBuffer ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public CatalogEntry (byte[] buffer, int offset)
  {
    memberName = Reader.getString (buffer, offset, 8);
    blockFrom = (int) Utility.getValue (buffer, offset + 8, 3);

    int extra = buffer[offset + 11] & 0xFF;
    int extraLength = 12 + (extra & 0x0F) * 2 + ((extra & 0x10) >> 4) * 32;
    switch (extra)
    {
      case 0x0F:
        basic (buffer, offset);

        init = Reader.getWord (buffer, offset + 28);
        mod = Reader.getWord (buffer, offset + 30);
        break;

      case 0x14:
        basic (buffer, offset);
        break;

      case 0x2B:
        break;

      case 0x2C:
        break;

      case 0x2E:
        break;

      case 0x31:
        break;

      case 0x37:
        break;

      case 0xB1:
        aliasName = Reader.getString (buffer, offset + 36, 8);
        break;

      case 0xB3:                // alias                          
        aliasName = Reader.getString (buffer, offset + 36, 8);
        break;

      case 0:
        break;

      default:
        System.out.printf ("********************** Unknown extra: %02X%n", extra);
    }

    directoryData = new byte[extraLength];
    System.arraycopy (buffer, offset, directoryData, 0, directoryData.length);

    if (false)
      System.out.printf ("%02X %-8s %06X %-129s %8s %8s%n", extra, getMemberName (),
          blockFrom, Reader.getHexString (buffer, offset + 12, length () - 12),
          getUserName (), getAliasName ());
  }

  // ---------------------------------------------------------------------------------//
  // basic
  // ---------------------------------------------------------------------------------//

  private void basic (byte[] buffer, int offset)
  {
    userName = Reader.getString (buffer, offset + 32, 8);
    size = Reader.getWord (buffer, offset + 26);

    vv = buffer[offset + 12] & 0xFF;
    mm = buffer[offset + 13] & 0xFF;

    date1 = getLocalDate (buffer, offset + 16);
    date2 = getLocalDate (buffer, offset + 20);
    time = String.format ("%02X:%02X:%02X", buffer[offset + 24], buffer[offset + 25],
        buffer[offset + 15]);

    if (false)
    {
      String vvmmText = String.format ("%02d.%02d", vv, mm);
      String date1Text = String.format ("%td %<tb %<tY", date1).replace (".", "");
      String date2Text = String.format ("%td %<tb %<tY", date2).replace (".", "");
      System.out.println (String.format ("%-8s  %6d  %6d %4d  %13s  %13s  %s  %5s  %s",
          memberName, size, init, mod, date1Text, date2Text, time, vvmmText, userName));
    }
  }

  // ---------------------------------------------------------------------------------//
  // length
  // ---------------------------------------------------------------------------------//

  int length ()
  {
    return directoryData.length;
  }

  // ---------------------------------------------------------------------------------//
  // getMemberName
  // ---------------------------------------------------------------------------------//

  public String getMemberName ()
  {
    return memberName;
  }

  // ---------------------------------------------------------------------------------//
  // getUserName
  // ---------------------------------------------------------------------------------//

  public String getUserName ()
  {
    return userName;
  }

  // ---------------------------------------------------------------------------------//
  // isAlias
  // ---------------------------------------------------------------------------------//

  public boolean isAlias ()
  {
    return !aliasName.isEmpty ();
  }

  // ---------------------------------------------------------------------------------//
  // getAliasName
  // ---------------------------------------------------------------------------------//

  public String getAliasName ()
  {
    return aliasName;
  }

  // ---------------------------------------------------------------------------------//
  // getSize
  // ---------------------------------------------------------------------------------//

  public int getSize ()
  {
    return size;
  }

  // ---------------------------------------------------------------------------------//
  // getDate
  // ---------------------------------------------------------------------------------//

  public LocalDate getDate ()
  {
    return date1;
  }

  // ---------------------------------------------------------------------------------//
  // getTime
  // ---------------------------------------------------------------------------------//

  public String getTime ()
  {
    return time;
  }

  // ---------------------------------------------------------------------------------//
  // getVersion
  // ---------------------------------------------------------------------------------//

  public String getVersion ()
  {
    if (vv == 0 & mm == 0)
      return "";
    return String.format ("%02d.%02d", vv, mm);
  }

  // ---------------------------------------------------------------------------------//
  // getBufferLength
  // ---------------------------------------------------------------------------------//

  public long getBufferLength ()
  {
    return bufferLength;
  }

  // ---------------------------------------------------------------------------------//
  // getDataLength
  // ---------------------------------------------------------------------------------//

  public long getDataLength ()
  {
    return dataLength;
  }

  // ---------------------------------------------------------------------------------//
  // addBlockPointerList
  // ---------------------------------------------------------------------------------//

  void addBlockPointerList (BlockPointerList blockPointerList)
  {
    logicalBuffer.addBlockPointerList (blockPointerList);

    blockPointerLists.add (blockPointerList);
    bufferLength += blockPointerList.getBufferLength ();
    dataLength += blockPointerList.getDataLength ();

    if (blockPointerLists.size () == 1
        && !blockPointerList.mysteryMatches (directoryData[0 + 10]))
      System.out.println ("Mismatch in " + memberName);
  }

  // ---------------------------------------------------------------------------------//
  // getText
  // ---------------------------------------------------------------------------------//

  public String getText ()
  {
    //    logicalBuffer.walk ();

    if (lines.size () == 0)
    {
      if (isAlias ())
        return "Alias of " + aliasName;
      if (blockPointerLists.size () == 0)
        return "No data";
      if (blockPointerLists.size () > 200)
        return partialDump ();
      if (blockPointerLists.get (0).isBinary ())
        return hexDump ();

      //      for (BlockPointerList blockPointerList : blockPointerLists)
      //        System.out.println (Utility.toHex (blockPointerList.getBuffer ()));
      for (BlockPointerList blockPointerList : blockPointerLists)
        addLines (blockPointerList);
    }

    StringBuilder text = new StringBuilder ();
    int lineNo = 0;
    for (String line : lines)
      text.append (String.format ("%05d0 %s%n", ++lineNo, line));
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // addLines
  // ---------------------------------------------------------------------------------//

  // this should be able to build lines directly from the original buffer
  private void addLines (BlockPointerList blockPointerList)
  {
    byte[] buffer = blockPointerList.getBuffer ();
    int dataLength = Reader.getWord (buffer, 10);       // bpl.dataLength
    int remainder = buffer.length - dataLength;
    if (remainder != 12 && remainder != 24)
      System.out.printf ("Unexpected remainder in %s: %d", memberName, remainder);

    int ptr = 12;
    while (dataLength > 0)
    {
      int len = Math.min (80, dataLength);
      lines.add (Reader.getString (buffer, ptr, len));
      ptr += len;
      dataLength -= len;
    }
  }

  private String hexDump ()
  {
    StringBuilder text = new StringBuilder ();

    for (int i = 0; i < blockPointerLists.size (); i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      if (bpl.getDataLength () > 0)
      {
        byte[] buffer = bpl.getBuffer ();
        int length = Reader.getWord (buffer, 10);
        text.append (Utility.toHex (buffer, 12, length));
        text.append ("\n\n");
      }
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // partialDump
  // ---------------------------------------------------------------------------------//

  private String partialDump ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (toString ());
    text.append ("\n\n");
    text.append ("Member data too large to display\n");
    int max = 5;
    text.append (
        "Showing first " + max + " of " + blockPointerLists.size () + " buffers\n\n");
    if (blockPointerLists.get (0).isXmit ())
      text.append ("Appears to be XMIT\n\n");
    for (int i = 0; i < max; i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      if (bpl.getDataLength () > 0)
      {
        byte[] buffer = bpl.getBuffer ();
        int length = Reader.getWord (buffer, 10);
        text.append (Utility.toHex (buffer, 12, length));
        if (i < max - 1)
          text.append ("\n\n");
      }
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // list
  // ---------------------------------------------------------------------------------//

  public void list ()
  {
    System.out.println (line);
    System.out.printf ("Member : %s%n", memberName);
    System.out.printf ("User   : %s%n", userName);
    System.out.println (line);

    int lineNo = 0;
    for (String line : lines)
      System.out.printf ("%05d0 %s%n", ++lineNo, line);
    System.out.println (line);
  }

  // ---------------------------------------------------------------------------------//
  // printLine
  // ---------------------------------------------------------------------------------//

  String getPrintLine ()
  {
    return String.format ("%-126s %8s %8s %5d %5d %5d",
        Reader.getHexString (directoryData), memberName, userName, size, init, mod);
  }

  // ---------------------------------------------------------------------------------//
  // getLocalDate
  // ---------------------------------------------------------------------------------//

  private LocalDate getLocalDate (byte[] buffer, int offset)
  {
    String date1 = String.format ("%02X%02X%02X%02X", buffer[offset], buffer[offset + 1],
        buffer[offset + 2], (buffer[offset + 3] & 0xF0));
    int d1 = Integer.parseInt (date1) / 10;
    return LocalDate.ofYearDay (1900 + d1 / 1000, d1 % 1000);
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("%8s  %8s  %8s  %06X ", memberName, userName, aliasName,
        blockFrom);
  }

  // ---------------------------------------------------------------------------------//
  // compareTo
  // ---------------------------------------------------------------------------------//

  @Override
  public int compareTo (CatalogEntry o)
  {
    if (this.blockFrom == o.blockFrom)
    {
      if (!this.isAlias () && o.isAlias ())
        return -1;
      if (!o.isAlias () && this.isAlias ())
        return 1;
      return this.memberName.compareTo (o.memberName);
    }

    return blockFrom < o.blockFrom ? -1 : 1;
  }
}
