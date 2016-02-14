package guitests;

import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import prefs.ConfigFileHandler;
import prefs.GlobalConfig;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.issuepanel.PanelControl;

public class PanelFocusTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true", "--bypasslogin=true");
    }

    @Override
    public void beforeStageStarts() {
        ConfigFileHandler configFileHandler =
            new ConfigFileHandler(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        GlobalConfig globalConfig = new GlobalConfig();

        PanelInfo test1 = new PanelInfo();
        PanelInfo test2 = new PanelInfo();
        PanelInfo test3 = new PanelInfo();
        List<PanelInfo> panels = new ArrayList<>();
        panels.add(test1);
        panels.add(test2);
        panels.add(test3);

        globalConfig.setPanelInfo(panels);
        configFileHandler.saveGlobalConfig(globalConfig);
    }

    // All tests kept in one method to control execution flow
    @Test
    public void panelFocusOnActionsTest() throws IllegalAccessException {

        PanelControl panelControl = (PanelControl) find("#dummy/dummy_col0").getParent();

        /**
         * Testing Panel Focus on Startup
         * ==============================
         * Only doing test for multiple panel at the start (not testing cases of
         * 1 recent panel or 0 recent panel) since it is only possible to launch
         * the application once for one test.
         * Having tests with multiple start ups will require each start up case
         * to be in its own test file.
         */
        // Check that there are multiple panels on startup (for precaution)
        assertEquals(3, panelControl.getPanelCount());

        // check that focus is on first panel
        // assertEquals(0, (int) panelControl.getCurrentlySelectedPanel().get());
        awaitCondition(() ->
            0 == panelControl.getCurrentlySelectedPanel().get().intValue());

        // Check that pressing F will go to second panel
        // This checks that no filter text field is in focus and panel
        // shortcut works at startup
        pushKeys(KeyCode.F);
        awaitCondition(() ->
            1 == panelControl.getCurrentlySelectedPanel().get().intValue());
        // assertEquals(1, (int) panelControl.getCurrentlySelectedPanel().get());

        // More shortcut checks to ensure the focus is always correct
        pushKeys(JUMP_TO_FILTER_BOX);
        awaitCondition(() ->
            1 == panelControl.getCurrentlySelectedPanel().get().intValue());
        // assertEquals(1, (int) panelControl.getCurrentlySelectedPanel().get());
        pushKeys(JUMP_TO_FIRST_ISSUE);
        awaitCondition(() ->
            1 == panelControl.getCurrentlySelectedPanel().get().intValue());
        // assertEquals(1, (int) panelControl.getCurrentlySelectedPanel().get());
        pushKeys(KeyCode.F);
        // System.out.println("CURRENTLY SELECTED: " +
        // panelControl.getCurrentlySelectedPanel());
        // sleep(10000); System.out.println("CURRENTLY SELECTED: " + panelControl.getCurrentlySelectedPanel());
        // sleep(10000);
        awaitCondition(() ->
            2 == panelControl.getCurrentlySelectedPanel().get().intValue());
        // assertEquals(2, (int) panelControl.getCurrentlySelectedPanel().get());


        /**
         * Testing Panel Focus on Creating Panels
         * ======================================
         */
        // test that upon creating panel on the right, focus is on the last panel
        // - this includes testing double space as the last panel might be
        //   colour focused but the real JavaFX focus is on first panel
        pushKeys(CREATE_RIGHT_PANEL);
        // PlatformEx.waitOnFxThread();
        awaitCondition(() -> panelControl.getCurrentlySelectedPanel().get().intValue() ==
            panelControl.getPanelCount() - 1);

        // assertEquals();
        type("  ");
        // PlatformEx.waitOnFxThread();
        awaitCondition(() -> panelControl.getCurrentlySelectedPanel().get().intValue() ==
            panelControl.getPanelCount() - 1);
        // assertEquals((int) panelControl.getCurrentlySelectedPanel().get(),
        // panelControl.getPanelCount() - 1);

        // test that upon creating panel on the left, focus is on the first panel
        // - same consideration as above
        pushKeys(CREATE_LEFT_PANEL);
        // PlatformEx.waitOnFxThread();
        awaitCondition(() -> 0 == panelControl.getCurrentlySelectedPanel().get().intValue());
        // assertEquals(0, (int) panelControl.getCurrentlySelectedPanel().get());
        type("  ");
        // PlatformEx.waitOnFxThread();
        awaitCondition(() -> 0 == panelControl.getCurrentlySelectedPanel().get().intValue());
        // assertEquals(0, (int) panelControl.getCurrentlySelectedPanel().get());

        /**
         * Testing First Panel is shown (i.e. scrollbar is set to left end)
         * and on focus upon Opening Board
         * ================================================================
         */
        // Setup:
        // 1. Save a board
        click("Boards");
        pushKeys(KeyCode.DOWN);
        pushKeys(KeyCode.DOWN);
        pushKeys(KeyCode.ENTER);
        ((TextField) find("#boardnameinput")).setText("Board 1");
        click("OK");
        // assertEquals(1, );
        awaitCondition(() -> 1 == panelControl.getNumberOfSavedBoards());
        // PlatformEx.waitOnFxThread();
        // 2. Create a new panel so that scroll bar is on the left
        pushKeys(CREATE_RIGHT_PANEL);
        // PlatformEx.waitOnFxThread();
        awaitCondition(() -> panelControl.getCurrentlySelectedPanel().get().intValue() ==
            panelControl.getPanelCount() - 1);
        // assertEquals();
        // 3. Open board
        pushKeys(SWITCH_BOARD);
        // PlatformEx.waitOnFxThread();

        // Check that first panel is on focus
        awaitCondition(() -> 0 == panelControl.getCurrentlySelectedPanel().get().intValue());
        // assertEquals();
        // Check that first panel is shown by checking scrollbar position
        ScrollPane panelsScrollPaneReflection =
            (ScrollPane) FieldUtils.readField(panelControl, "panelsScrollPane", true);
        // awaitCondition(() -> 0 == (int) panelControl.getCurrentlySelectedPanel().get());
        assertEquals(0, panelsScrollPaneReflection.getHvalue(), 0.001);
    }
}