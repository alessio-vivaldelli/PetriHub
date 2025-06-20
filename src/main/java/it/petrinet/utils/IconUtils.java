package it.petrinet.utils;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Labeled;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.Optional;

/**
 * Classe di utilità per creare e applicare icone ai controlli JavaFX.
 * Permette di impostare dimensioni personalizzate e di ricolorare le icone (PNG).
 * La funzione di colorazione funziona in modo ottimale con icone bianche o in scala di grigi.
 */
public final class IconUtils {

    private static final String BASE_PATH = "/assets/icons/";
    private static final int DEFAULT_ICON_SIZE = 24;
    private static final int DEFAULT_TEXT_GAP = 5;

    /**
     * Costruttore privato per prevenire l'istanziazione.
     */
    private IconUtils() {}

    // --- METODI PUBBLICI PRINCIPALI ---

    /**
     * Imposta un'icona su un controllo Labeled (Button, Label, etc.) con dimensioni di default e posizione standard (sinistra).
     *
     * @param node Il controllo su cui impostare l'icona.
     * @param iconName Il nome del file dell'icona (es. "home.png").
     */
    public static void setIcon(Labeled node, String iconName) {
        setIcon(node, iconName, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE, null, ContentDisplay.LEFT);
    }

    /**
     * Imposta un'icona su un controllo Labeled con una dimensione personalizzata (quadrata) e posizione standard (sinistra).
     *
     * @param node Il controllo su cui impostare l'icona.
     * @param iconName Il nome del file dell'icona.
     * @param size La larghezza e l'altezza desiderate per l'icona.
     */
    public static void setIcon(Labeled node, String iconName, double size) {
        setIcon(node, iconName, size, size, null, ContentDisplay.LEFT);
    }

    /**
     * Imposta un'icona su un controllo Labeled, ricolorandola e con posizione standard (sinistra).
     *
     * @param node Il controllo su cui impostare l'icona.
     * @param iconName Il nome del file dell'icona.
     * @param color Il colore con cui tingere l'icona.
     */
    public static void setIcon(Labeled node, String iconName, Color color) {
        setIcon(node, iconName, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE, color, ContentDisplay.LEFT);
    }

    /**
     * Imposta un'icona su un controllo Labeled con dimensioni e colore personalizzati, e posizione standard (sinistra).
     *
     * @param node Il controllo su cui impostare l'icona.
     * @param iconName Il nome del file dell'icona.
     * @param width La larghezza desiderata per l'icona.
     * @param height L'altezza desiderata per l'icona.
     * @param color Il colore con cui tingere l'icona (null per non applicare alcun colore).
     */
    public static void setIcon(Labeled node, String iconName, double width, double height, Color color) {
        setIcon(node, iconName, width, height, color, ContentDisplay.LEFT);
    }

    /**
     * Imposta un'icona su un controllo Labeled con dimensioni, colore e posizione personalizzati.
     * Questo è il metodo più specifico che viene richiamato dagli altri.
     *
     * @param node Il controllo su cui impostare l'icona.
     * @param iconName Il nome del file dell'icona.
     * @param width La larghezza desiderata per l'icona.
     * @param height L'altezza desiderata per l'icona.
     * @param color Il colore con cui tingere l'icona (null per non applicare alcun colore).
     * @param contentDisplay La posizione dell'icona rispetto al testo (es. ContentDisplay.LEFT, ContentDisplay.RIGHT).
     */
    public static void setIcon(Labeled node, String iconName, double width, double height, Color color, ContentDisplay contentDisplay) {
        createIconView(iconName, width, height, color).ifPresent(iconView -> {
            node.setGraphic(iconView);
            node.setGraphicTextGap(DEFAULT_TEXT_GAP);
            node.setContentDisplay(contentDisplay); // Imposta la posizione dell'icona
        });
    }

    // --- LOGICA INTERNA ---

    /**
     * Crea un ImageView a partire da un file di icona, con la possibilità di ridimensionare e ricolorare.
     *
     * @return Un Optional contenente l'ImageView se l'icona è stata caricata correttamente, altrimenti un Optional vuoto.
     */
    private static Optional<ImageView> createIconView(String iconFileName, double width, double height, Color color) {
        String fullPath = BASE_PATH + adjustPath(iconFileName);
        try (InputStream is = IconUtils.class.getResourceAsStream(fullPath)) {
            if (is == null) {
                System.err.printf("ERROR: Icon '%s' not found in classpath at '%s'%n", iconFileName, fullPath);
                return Optional.empty();
            }

            Image icon = new Image(is);
            ImageView iconView = new ImageView(icon);
            iconView.setFitWidth(width);
            iconView.setFitHeight(height);
            iconView.setPreserveRatio(true);

            // Applica l'effetto di colore se un colore è stato specificato
            if (color != null) {
                applyColorEffect(iconView, width, height, color);
            }

            return Optional.of(iconView);

        } catch (Exception e) {
            System.err.printf("ERROR loading icon '%s': %s%n", iconFileName, e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Applica un effetto di "tinta" all'ImageView.
     * Questa implementazione usa BlendMode.SRC_ATOP per colorare l'icona
     * mantenendo la trasparenza originale, ed è ideale per icone bianche o in scala di grigi.
     */
    private static void applyColorEffect(ImageView iconView, double width, double height, Color color) {
        // Il ColorInput crea un rettangolo del colore specificato
        ColorInput colorInput = new ColorInput(0, 0, width, height, color);

        // Il BlendMode.SRC_ATOP sovrappone la sorgente (colorInput) alla destinazione (iconView)
        // solo dove la destinazione è opaca, preservando l'alpha della destinazione.
        // Se l'icona è bianca/grigia con aree trasparenti, questo la "tinge" efficacemente.
        Blend blendEffect = new Blend(BlendMode.SRC_ATOP, null, colorInput);
        iconView.setEffect(blendEffect);
    }
    /**
     * Aggiunge l'estensione .png se non è presente.
     */
    private static String adjustPath(String iconFileName) {
        return iconFileName.endsWith(".png") ? iconFileName : iconFileName + ".png";
    }
}