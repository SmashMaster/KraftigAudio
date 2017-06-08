package kraftig.game;

import com.samrj.devil.ui.AtlasFont;

public class UI
{
    private final AtlasFont font;
    
    UI() throws Exception
    {
        font = new AtlasFont("kraftig/res/fonts/", "hud.fnt");
    }
}
