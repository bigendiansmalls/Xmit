package com.bytezone.xmit.gui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.bytezone.xmit.CatalogEntry;
import com.bytezone.xmit.Dataset;
import com.bytezone.xmit.PdsDataset;
import com.bytezone.xmit.Reader;
import com.bytezone.xmit.Utility.FileType;
import com.bytezone.xmit.textunit.Dsorg.Org;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.util.Callback;

// ---------------------------------------------------------------------------------//
class XmitTable extends TableView<CatalogEntryItem>
    implements TreeItemSelectionListener, FontChangeListener
// ---------------------------------------------------------------------------------//
{
  private static final String PREFS_LAST_MEMBER_INDEX = "LastMemberIndex";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final List<TableItemSelectionListener> listeners = new ArrayList<> ();
  private final ObservableList<CatalogEntryItem> items =
      FXCollections.observableArrayList ();

  private Dataset dataset;
  private final Map<Reader, String> selectedMembers = new HashMap<> ();
  private Font font;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  XmitTable ()
  {
    //    setStyle ("-fx-font-size: 13; -fx-font-family: monospaced");
    //    setFixedCellSize (12);
    setItems (items);

    addString ("Member", "MemberName", 100, "CENTER-LEFT");
    addString ("Id", "UserName", 100, "CENTER-LEFT");
    addNumber ("Bytes", "Bytes", 90);
    addNumber ("Size", "Size", 70);
    addNumber ("Init", "Init", 70);
    addLocalDate ("Created", "DateCreated", 100);
    addLocalDate ("Modified", "DateModified", 100);
    addString ("Time", "Time", 90, "CENTER");
    addFileType ("Type", "Type", 50, "CENTER");
    addString ("ver.mod", "Version", 70, "CENTER");
    addString ("Alias", "AliasName", 100, "CENTER-LEFT");

    getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, catalogEntryItem) ->
        {
          if (catalogEntryItem == null)
            return;

          CatalogEntry catalogEntry = catalogEntryItem.getCatalogEntry ();
          selectedMembers.put (dataset.getReader (), catalogEntry.getMemberName ());
          for (TableItemSelectionListener listener : listeners)
            listener.tableItemSelected (catalogEntry);
        });
  }

  // ---------------------------------------------------------------------------------//
  // addNumber
  // ---------------------------------------------------------------------------------//

  TableColumn<CatalogEntryItem, Number> addNumber (String heading, String name, int width)
  {
    TableColumn<CatalogEntryItem, Number> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (numberCellFactory ());
    column.setMinWidth (width);
    getColumns ().add (column);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  // addString
  // ---------------------------------------------------------------------------------//

  TableColumn<CatalogEntryItem, String> addString (String heading, String name, int width,
      String alignment)
  {
    TableColumn<CatalogEntryItem, String> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (stringCellFactory (alignment));
    column.setPrefWidth (width);
    getColumns ().add (column);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  // addLocalDate
  // ---------------------------------------------------------------------------------//

  TableColumn<CatalogEntryItem, LocalDate> addLocalDate (String heading, String name,
      int width)
  {
    TableColumn<CatalogEntryItem, LocalDate> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (localDateCellFactory ());
    column.setPrefWidth (width);
    getColumns ().add (column);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  // addFileType
  // ---------------------------------------------------------------------------------//

  TableColumn<CatalogEntryItem, FileType> addFileType (String heading, String name,
      int width, String alignment)
  {
    TableColumn<CatalogEntryItem, FileType> column = new TableColumn<> (heading);
    column.setCellValueFactory (new PropertyValueFactory<> (name));
    column.setCellFactory (fileTypeCellFactory (alignment));
    column.setPrefWidth (width);
    getColumns ().add (column);
    return column;
  }

  // ---------------------------------------------------------------------------------//
  // numberCellFactory
  // ---------------------------------------------------------------------------------//

  Callback<TableColumn<CatalogEntryItem, Number>, TableCell<CatalogEntryItem, Number>>
      numberCellFactory ()
  {
    return new Callback<TableColumn<CatalogEntryItem, Number>, TableCell<CatalogEntryItem, Number>> ()
    {
      @Override
      public TableCell<CatalogEntryItem, Number>
          call (TableColumn<CatalogEntryItem, Number> param)
      {
        TableCell<CatalogEntryItem, Number> cell = new TableCell<> ()
        {
          @Override
          public void updateItem (final Number item, boolean empty)
          {
            super.updateItem (item, empty);
            setStyle ("-fx-alignment: CENTER-RIGHT;");
            if (item == null || empty)
              setText (null);
            else
            {
              setText (String.format ("%,d", item));
              setFont (font);
            }
          }
        };
        return cell;
      }
    };
  }

  // ---------------------------------------------------------------------------------//
  // stringCellFactory
  // ---------------------------------------------------------------------------------//

  Callback<TableColumn<CatalogEntryItem, String>, TableCell<CatalogEntryItem, String>>
      stringCellFactory (String alignment)
  {
    return new Callback<TableColumn<CatalogEntryItem, String>, TableCell<CatalogEntryItem, String>> ()
    {
      @Override
      public TableCell<CatalogEntryItem, String>
          call (TableColumn<CatalogEntryItem, String> param)
      {
        TableCell<CatalogEntryItem, String> cell = new TableCell<> ()
        {
          @Override
          public void updateItem (final String item, boolean empty)
          {
            super.updateItem (item, empty);
            setStyle ("-fx-alignment: " + alignment + ";");
            if (item == null || empty)
              setText (null);
            else
            {
              setText (item);
              setFont (font);
            }
          }
        };
        return cell;
      }
    };
  }

  // ---------------------------------------------------------------------------------//
  // localDateCellFactory
  // ---------------------------------------------------------------------------------//

  Callback<TableColumn<CatalogEntryItem, LocalDate>, TableCell<CatalogEntryItem, LocalDate>>
      localDateCellFactory ()
  {
    return new Callback<TableColumn<CatalogEntryItem, LocalDate>, TableCell<CatalogEntryItem, LocalDate>> ()
    {
      @Override
      public TableCell<CatalogEntryItem, LocalDate>
          call (TableColumn<CatalogEntryItem, LocalDate> param)
      {
        TableCell<CatalogEntryItem, LocalDate> cell =
            new TableCell<CatalogEntryItem, LocalDate> ()
            {
              @Override
              public void updateItem (final LocalDate item, boolean empty)
              {
                super.updateItem (item, empty);
                setStyle ("-fx-alignment: CENTER;");
                if (item == null || empty)
                  setText (null);
                else
                {
                  setText (String.format ("%s", item));
                  setFont (font);
                }
              }
            };
        return cell;
      }
    };
  }

  // ---------------------------------------------------------------------------------//
  // fileTypeCellFactory
  // ---------------------------------------------------------------------------------//

  Callback<TableColumn<CatalogEntryItem, FileType>, TableCell<CatalogEntryItem, FileType>>
      fileTypeCellFactory (String alignment)
  {
    return new Callback<TableColumn<CatalogEntryItem, FileType>, TableCell<CatalogEntryItem, FileType>> ()
    {
      @Override
      public TableCell<CatalogEntryItem, FileType>
          call (TableColumn<CatalogEntryItem, FileType> param)
      {
        TableCell<CatalogEntryItem, FileType> cell =
            new TableCell<CatalogEntryItem, FileType> ()
            {
              @Override
              public void updateItem (final FileType item, boolean empty)
              {
                super.updateItem (item, empty);
                setStyle ("-fx-alignment: " + alignment + ";");
                if (item == null || empty)
                  setText (null);
                else
                {
                  setText (item == FileType.BIN ? "" : String.format ("%s", item));
                  setFont (font);
                }
              }
            };
        return cell;
      }
    };
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
    prefs.putInt (PREFS_LAST_MEMBER_INDEX, getSelectionModel ().getSelectedIndex ());
  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  void restore ()
  {
    int index = prefs.getInt (PREFS_LAST_MEMBER_INDEX, 0);
    select (index);
  }

  // ---------------------------------------------------------------------------------//
  // addListener
  // ---------------------------------------------------------------------------------//

  void addListener (TableItemSelectionListener listener)
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  // removeListener
  // ---------------------------------------------------------------------------------//

  void removeListener (TableItemSelectionListener listener)
  {
    listeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  // treeItemSelected
  // ---------------------------------------------------------------------------------//

  @Override
  public void treeItemSelected (Dataset dataset, String name)
  {
    this.dataset = dataset;

    items.clear ();
    if (dataset != null && dataset.getDisposition ().getOrg () == Org.PDS)
    {
      for (CatalogEntry catalogEntry : ((PdsDataset) dataset).getCatalogEntries ())
        items.add (new CatalogEntryItem (catalogEntry));

      select (selectedMembers.containsKey (dataset.getReader ())
          ? memberIndex (selectedMembers.get (dataset.getReader ())) : 0);
    }
  }

  // ---------------------------------------------------------------------------------//
  // memberIndex
  // ---------------------------------------------------------------------------------//

  private int memberIndex (String memberName)
  {
    int index = 0;
    for (CatalogEntry catalogEntry : ((PdsDataset) dataset).getCatalogEntries ())
    {
      if (memberName.equals (catalogEntry.getMemberName ()))
        return index;
      ++index;
    }
    return 0;
  }

  // ---------------------------------------------------------------------------------//
  // select
  // ---------------------------------------------------------------------------------//

  private void select (int index)
  {
    getFocusModel ().focus (index);
    getSelectionModel ().select (index);
    scrollTo (index);
  }

  // ---------------------------------------------------------------------------------//
  // setFont
  // ---------------------------------------------------------------------------------//

  @Override
  public void setFont (Font font)
  {
    this.font = font;
    refresh ();
  }
}
