package it.petrinet.utils;

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
     * Imposta un'icona su un controllo Labeled (Button, Label, etc.) con dimensioni di default.
     *
     * @param node Il controllo su cui impostare l'icona.
     * @param iconName Il nome del file dell'icona (es. "home.png").
     */
    public static void setIcon(Labeled node, String iconName) {
        setIcon(node, iconName, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE, null);
    }

    /**
     * Imposta un'icona su un controllo Labeled con una dimensione personalizzata (quadrata).
     *
     * @param node Il controllo su cui impostare l'icona.
     * @param iconName Il nome del file dell'icona.
     * @param size La larghezza e l'altezza desiderate per l'icona.
     */
    public static void setIcon(Labeled node, String iconName, double size) {
        setIcon(node, iconName, size, size, null);
    }

    /**
     * Imposta un'icona su un controllo Labeled, ricolorandola.
     *
     * @param node Il controllo su cui impostare l'icona.
     * @param iconName Il nome del file dell'icona.
     * @param color Il colore con cui tingere l'icona.
     */
    public static void setIcon(Labeled node, String iconName, Color color) {
        setIcon(node, iconName, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE, color);
    }

    /**
     * Imposta un'icona su un controllo Labeled con dimensioni e colore personalizzati.
     * Questo è il metodo più specifico che viene richiamato dagli altri.
     *
     * @param node Il controllo su cui impostare l'icona.
     * @param iconName Il nome del file dell'icona.
     * @param width La larghezza desiderata per l'icona.
     * @param height L'altezza desiderata per l'icona.
     * @param color Il colore con cui tingere l'icona (null per non applicare alcun colore).
     */
    public static void setIcon(Labeled node, String iconName, double width, double height, Color color) {
        createIconView(iconName, width, height, color).ifPresent(iconView -> {
            node.setGraphic(iconView);
            node.setGraphicTextGap(DEFAULT_TEXT_GAP);
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
     * Funziona moltiplicando un livello di colore con l'immagine originale.
     */
    private static void applyColorEffect(ImageView iconView, double width, double height, Color color) {
        ColorInput colorInput = new ColorInput(0, 0, width, height, color);
        Blend blendEffect = new Blend(BlendMode.MULTIPLY, null, colorInput);
        iconView.setEffect(blendEffect);
    }

    /**
     * Aggiunge l'estensione .png se non è presente.
     */
    private static String adjustPath(String iconFileName) {
        return iconFileName.endsWith(".png") ? iconFileName : iconFileName + ".png";
    }
}