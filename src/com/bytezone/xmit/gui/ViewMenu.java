package com.bytezone.xmit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

// ---------------------------------------------------------------------------------//
class ViewMenu
//---------------------------------------------------------------------------------//
{
  private static final String PREFS_SHOW_LINES = "ShowLines";
  private static final String PREFS_STRIP_LINES = "StripLines";
  private static final String PREFS_TRUNCATE = "Truncate";
  private static final String PREFS_SHOW_HEADERS = "ShowHeaders";
  private static final String PREFS_SHOW_BLOCKS = "ShowBlocks";
  private static final String PREFS_SHOW_HEX = "ShowHex";

  private static final String PREFS_CODE_PAGE = "CodePage";
  private static final String PREFS_EURO_PAGE = "EuroPage";

  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());

  private final List<ShowLinesListener> showLinesListeners = new ArrayList<> ();
  private final List<CodePageSelectedListener> codePageListeners = new ArrayList<> ();
  private final XmitApp xmitApp;

  private final Menu viewMenu = new Menu ("View");
  private final CheckMenuItem showLinesMenuItem = new CheckMenuItem ("Add Line Numbers");
  private final CheckMenuItem stripLinesMenuItem =
      new CheckMenuItem ("Strip Line Numbers");
  private final CheckMenuItem truncateMenuItem = new CheckMenuItem ("Truncate Column 1");
  private final MenuItem fontMenuItem = new CheckMenuItem ("Set Font...");
  private final CheckMenuItem headersMenuItem = new CheckMenuItem ("Headers tab");
  private final CheckMenuItem blocksMenuItem = new CheckMenuItem ("Blocks tab");
  private final CheckMenuItem hexMenuItem = new CheckMenuItem ("Hex tab");

  private final String[][] codePageNames =
      { { "CP037", "CP1140" }, { "CP273", "CP1141" }, { "CP285", "CP1146" },
        { "CP297", "CP1147" }, { "CP500", "CP1148" }, { "CP1047", "CP924" },
        { "USER1", "USER1" } };
  private final KeyCode[] keyCodes =
      { KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4, KeyCode.DIGIT5,
        KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8 };

  private final ToggleGroup toggleGroup = new ToggleGroup ();
  List<RadioMenuItem> codePageMenuItems = new ArrayList<> ();
  private final CheckMenuItem euroMenuItem = new CheckMenuItem ("Euro update");

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public ViewMenu (XmitApp xmitApp, TreeView<XmitFile> tree, FontManager fontManager)
  {
    this.xmitApp = xmitApp;

    for (int i = 0; i < codePageNames.length; i++)
      codePageMenuItems.add (setMenuItem (codePageNames[i][0], keyCodes[i]));

    viewMenu.getItems ().addAll (showLinesMenuItem, stripLinesMenuItem, truncateMenuItem,
        fontMenuItem, new SeparatorMenuItem (), headersMenuItem, blocksMenuItem,
        hexMenuItem, new SeparatorMenuItem ());
    for (RadioMenuItem item : codePageMenuItems)
      viewMenu.getItems ().add (item);
    viewMenu.getItems ().addAll (new SeparatorMenuItem (), euroMenuItem);

    showLinesMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.L, KeyCombination.SHORTCUT_DOWN));
    stripLinesMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.S, KeyCombination.SHORTCUT_DOWN));
    truncateMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.T, KeyCombination.SHORTCUT_DOWN));
    fontMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.F, KeyCombination.SHORTCUT_DOWN));
    euroMenuItem.setAccelerator (
        new KeyCodeCombination (KeyCode.DIGIT9, KeyCombination.SHORTCUT_DOWN));

    showLinesMenuItem.setOnAction (e -> notifyLinesListeners ());
    stripLinesMenuItem.setOnAction (e -> notifyLinesListeners ());
    truncateMenuItem.setOnAction (e -> notifyLinesListeners ());
    fontMenuItem.setOnAction (e -> fontManager.showWindow ());

    headersMenuItem.setOnAction (e -> setTabs ());
    blocksMenuItem.setOnAction (e -> setTabs ());
    hexMenuItem.setOnAction (e -> setTabs ());
    euroMenuItem.setOnAction (e -> setEuroAndNotifyListeners ());
  }

  // ---------------------------------------------------------------------------------//
  // setMenuItem
  // ---------------------------------------------------------------------------------//

  private RadioMenuItem setMenuItem (String name, KeyCode keyCode)
  {
    RadioMenuItem menuItem = new RadioMenuItem (name);
    menuItem.setToggleGroup (toggleGroup);
    menuItem.setUserData (name);
    menuItem.setOnAction (e -> notifyCodePageListeners ());
    menuItem
        .setAccelerator (new KeyCodeCombination (keyCode, KeyCombination.SHORTCUT_DOWN));
    return menuItem;
  }

  // ---------------------------------------------------------------------------------//
  // notifyLinesListeners
  // ---------------------------------------------------------------------------------//

  private void notifyLinesListeners ()
  {
    for (ShowLinesListener listener : showLinesListeners)
      listener.showLinesSelected (showLinesMenuItem.isSelected (),
          stripLinesMenuItem.isSelected (), truncateMenuItem.isSelected ());
  }

  // ---------------------------------------------------------------------------------//
  // notifyCodePageListeners
  // ---------------------------------------------------------------------------------//

  private void notifyCodePageListeners ()
  {
    Toggle toggle = toggleGroup.getSelectedToggle ();
    if (toggle == null)
    {
      System.out.println ("Nothing selected");        // windows bug
    }
    else
    {
      Object o = toggle.getUserData ();
      String codePageName = o.toString ();
      for (CodePageSelectedListener listener : codePageListeners)
        listener.selectCodePage (codePageName);
    }
  }

  // ---------------------------------------------------------------------------------//
  // setEuroAndNotifyListeners
  // ---------------------------------------------------------------------------------//

  private void setEuroAndNotifyListeners ()
  {
    int j = euroMenuItem.isSelected () ? 1 : 0;
    for (int i = 0; i < codePageNames.length; i++)
    {
      codePageMenuItems.get (i).setText (codePageNames[i][j]);
      codePageMenuItems.get (i).setUserData (codePageNames[i][j]);
    }
    notifyCodePageListeners ();
  }

  // ---------------------------------------------------------------------------------//
  // setTabs
  // ---------------------------------------------------------------------------------//

  private void setTabs ()
  {
    xmitApp.setTabVisible (headersMenuItem.isSelected (), blocksMenuItem.isSelected (),
        hexMenuItem.isSelected ());
  }

  // ---------------------------------------------------------------------------------//
  // restore
  // ---------------------------------------------------------------------------------//

  void restore ()
  {
    showLinesMenuItem.setSelected (prefs.getBoolean (PREFS_SHOW_LINES, false));
    stripLinesMenuItem.setSelected (prefs.getBoolean (PREFS_STRIP_LINES, false));
    truncateMenuItem.setSelected (prefs.getBoolean (PREFS_TRUNCATE, false));
    notifyLinesListeners ();

    headersMenuItem.setSelected (prefs.getBoolean (PREFS_SHOW_HEADERS, false));
    blocksMenuItem.setSelected (prefs.getBoolean (PREFS_SHOW_BLOCKS, false));
    hexMenuItem.setSelected (prefs.getBoolean (PREFS_SHOW_HEX, false));
    setTabs ();

    euroMenuItem.setSelected (prefs.getBoolean (PREFS_EURO_PAGE, false));

    int j = euroMenuItem.isSelected () ? 1 : 0;
    String codePageName = prefs.get (PREFS_CODE_PAGE, codePageNames[0][j]);

    for (int i = 0; i < codePageNames.length; i++)
      if (codePageNames[i][j].equals (codePageName))
      {
        toggleGroup.selectToggle (codePageMenuItems.get (i));
        break;
      }

    setEuroAndNotifyListeners ();
  }

  // ---------------------------------------------------------------------------------//
  // exit
  // ---------------------------------------------------------------------------------//

  void exit ()
  {
    prefs.putBoolean (PREFS_SHOW_LINES, showLinesMenuItem.isSelected ());
    prefs.putBoolean (PREFS_STRIP_LINES, stripLinesMenuItem.isSelected ());
    prefs.putBoolean (PREFS_TRUNCATE, truncateMenuItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_HEADERS, headersMenuItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_BLOCKS, blocksMenuItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_HEX, hexMenuItem.isSelected ());
    prefs.putBoolean (PREFS_EURO_PAGE, euroMenuItem.isSelected ());

    Toggle toggle = toggleGroup.getSelectedToggle ();
    if (toggle != null)
      prefs.put (PREFS_CODE_PAGE, toggle.getUserData ().toString ());
  }

  // ---------------------------------------------------------------------------------//
  // addShowLinesListener
  // ---------------------------------------------------------------------------------//

  public void addShowLinesListener (ShowLinesListener listener)
  {
    if (!showLinesListeners.contains (listener))
      showLinesListeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  // addCodePageListener
  // ---------------------------------------------------------------------------------//

  public void addCodePageListener (CodePageSelectedListener listener)
  {
    if (!codePageListeners.contains (listener))
      codePageListeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  // getMenu
  // ---------------------------------------------------------------------------------//

  Menu getMenu ()
  {
    return viewMenu;
  }
}
