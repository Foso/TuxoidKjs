import data.savegame.SaveGameDataSource
import ui.menu.Option
import ui.menu.SubMenu

class Presenter(val view: Contract.View, val saveGameDataSource: SaveGameDataSource) : Contract.Presenter {

    fun HAS_STORAGE(): Boolean {
        return true
    }

    override fun init() {

        val arr_options1 = arrayOf(
            Option(false, 0, "New", "F2", 0, { true }),
            Option(false, 0, "Load Game...", "", 1, { HAS_STORAGE() }),
            Option(false, 0, "Save", "", 2, { saveGameDataSource.getSaveGame().progressed && HAS_STORAGE() }),
            Option(false, 1, "Pause", "", 3, { true })
        )

        val arr_options2 = arrayOf(
            Option(false, 1, "Single steps", "F5", 4, { true }),
            Option(false, 1, "Sound", "", 5, { true }),
            Option(true, 0, "", "", -1, { true }),
            Option(false, 0, "Load Level", "", 6, { HAS_STORAGE() }),
            Option(
                false,
                0,
                "Change Password",
                "",
                7,
                { saveGameDataSource.getSaveGame().username !== null && HAS_STORAGE() }),
            Option(true, 0, "", "", -1, { true }),
            Option(
                false, 0, "Charts", "", 8, { HAS_STORAGE() })
        )

        val sub_m1 = SubMenu(43, 100, "Game", arr_options1);
        val sub_m2 = SubMenu(55, 150, "Options", arr_options2);

        view.setupMenu(arrayOf(sub_m1, sub_m2))
    }

    override fun shutdown() {


    }

}