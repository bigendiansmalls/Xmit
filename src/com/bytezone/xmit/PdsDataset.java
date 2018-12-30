package com.bytezone.xmit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.bytezone.xmit.textunit.Dsorg.Org;

public class PdsDataset extends Dataset
{
  private static final int DIR_BLOCK_LENGTH = 0x114;

  private int catalogEndBlock = 0;
  private final List<CatalogEntry> catalogEntries = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  PdsDataset (Org org, int lrecl)
  {
    super (org, lrecl);
  }

  // ---------------------------------------------------------------------------------//
  // getCatalogEntries
  // ---------------------------------------------------------------------------------//

  public List<CatalogEntry> getCatalogEntries ()
  {
    return catalogEntries;
  }

  // ---------------------------------------------------------------------------------//
  // getXmitFiles
  // ---------------------------------------------------------------------------------//

  public List<CatalogEntry> getMembers ()
  {
    List<CatalogEntry> xmitFiles = new ArrayList<> ();
    for (CatalogEntry catalogEntry : catalogEntries)
      if (catalogEntry.isXmit ())
        xmitFiles.add (catalogEntry);
    return xmitFiles;
  }

  // ---------------------------------------------------------------------------------//
  // processPDS
  // ---------------------------------------------------------------------------------//

  void processPDS ()
  {
    boolean inCatalog = true;

    // skip first two BlockPointerList entries
    // read catalog data as raw data
    // convert remaining entries to BlockPointers with the headers removed
    for (int i = 2; i < blockPointerLists.size (); i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      if (inCatalog)
      {
        inCatalog = addCatalogEntries (bpl.getRawBuffer ());
        if (!inCatalog)
          catalogEndBlock = i;
      }
      else
        bpl.createDataBlocks ();       // create new BlockPointers
    }

    // assign new BlockPointer lists to CatalogEntries
    List<CatalogEntry> sortedCatalogEntries = new ArrayList<> (catalogEntries);
    Collections.sort (sortedCatalogEntries);

    Map<Integer, CatalogEntry> offsets = new TreeMap<> ();
    for (CatalogEntry catalogEntry : sortedCatalogEntries)
      if (!offsets.containsKey (catalogEntry.getOffset ()))
        offsets.put (catalogEntry.getOffset (), catalogEntry);

    List<CatalogEntry> uniqueCatalogEntries = new ArrayList<> ();
    for (CatalogEntry catalogEntry : offsets.values ())
      uniqueCatalogEntries.add (catalogEntry);

    // assign BlockPointerLists to CatalogEntries
    if (blockPointerLists.get (catalogEndBlock + 1).isPDSE ())
      assignPdsExtendedBlocks (uniqueCatalogEntries);
    else
      assignPdsBlocks (uniqueCatalogEntries);
  }

  // ---------------------------------------------------------------------------------//
  // assignPdsBlocks
  // ---------------------------------------------------------------------------------//

  private void assignPdsBlocks (List<CatalogEntry> uniqueCatalogEntries)
  {
    int currentMember = 0;
    for (int i = catalogEndBlock + 1; i < blockPointerLists.size (); i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);
      CatalogEntry catalogEntry = uniqueCatalogEntries.get (currentMember);
      if (!catalogEntry.addBlockPointerList (bpl))
        break;

      if (bpl.isLastBlock ())
        ++currentMember;
    }
  }

  // ---------------------------------------------------------------------------------//
  // assignPdsExtendedBlocks
  // ---------------------------------------------------------------------------------//

  private void assignPdsExtendedBlocks (List<CatalogEntry> uniqueCatalogEntries)
  {
    int lastOffset = -1;
    int currentMember = -1;

    for (int i = catalogEndBlock + 2; i < blockPointerLists.size (); i++)
    {
      BlockPointerList bpl = blockPointerLists.get (i);

      int offset = bpl.getOffset ();
      if (lastOffset != offset)
      {
        ++currentMember;
        lastOffset = offset;
      }
      CatalogEntry catalogEntry = uniqueCatalogEntries.get (currentMember);
      if (catalogEntry.getOffset () == offset)
      {
        catalogEntry.setPdse (true);
        catalogEntry.addBlockPointerList (bpl);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // addCatalogEntries
  // ---------------------------------------------------------------------------------//

  boolean addCatalogEntries (byte[] buffer)
  {
    int ptr = 0;
    while (ptr + 22 < buffer.length)
    {
      int ptr2 = ptr + 22;

      while (true)
      {
        if (buffer[ptr2] == (byte) 0xFF)
          return false;                                     // member list finished

        CatalogEntry catalogEntry = new CatalogEntry (buffer, ptr2, lrecl);
        catalogEntries.add (catalogEntry);

        // check for last member
        if (Utility.matches (buffer, ptr2, buffer, ptr + 12, 8))
          break;

        ptr2 += catalogEntry.length ();
      }

      ptr += DIR_BLOCK_LENGTH;
    }

    return true;                                            // member list not finished
  }
}