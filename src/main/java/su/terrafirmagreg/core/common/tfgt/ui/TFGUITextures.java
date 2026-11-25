package su.terrafirmagreg.core.common.tfgt.ui;

import brachy.modularui.drawable.ColorType;
import brachy.modularui.drawable.UITexture;

import su.terrafirmagreg.core.TFGCore;

// Progress Bars for recipe types.
public class TFGUITextures {

    public static final UITexture PROGRESS_BAR_DNA = UITexture.fullImage(
            "tfg:textures/gui/progress_bar/progress_bar_dna.png");

    public static final UITexture PROGRESS_BAR_PETRI = UITexture.fullImage(
            "tfg:textures/gui/progress_bar/progress_bar_petri.png");

    public static final UITexture PROGRESS_BAR_EGH = UITexture.fullImage(
            "tfg:textures/gui/progress_bar/progress_bar_egh.png");

    public static final UITexture PROGRESS_BAR_FISH = UITexture.fullImage(
            "tfg:textures/gui/progress_bar/progress_bar_fish.png");

    public static final UITexture PROGRESS_BAR_BOULE = UITexture.fullImage(
            "tfg:textures/gui/progress_bar/progress_bar_boule.png");

    public static final UITexture BUTTON_FIND_LEAK = UITexture.fullImage(TFGCore.MOD_ID,
            "textures/gui/widgets/button_find_leak.png", ColorType.DEFAULT);

    public static final UITexture BUTTON_FILL_SUIT = UITexture.fullImage(TFGCore.MOD_ID,
            "textures/gui/widgets/button_fill_suit.png", ColorType.DEFAULT);
}
