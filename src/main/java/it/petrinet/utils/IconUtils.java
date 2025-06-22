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
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utility class for creating and applying icons to JavaFX controls.
 * Allows setting custom dimensions and recoloring icons (PNG).
 * The coloring function works optimally with white or grayscale icons.
 */
public final class IconUtils {

    private static final Logger LOGGER = Logger.getLogger(IconUtils.class.getName());

    // Constants
    private static final String BASE_PATH = "/assets/icons/";
    private static final String ICON_EXTENSION = ".png";
    public static final int DEFAULT_ICON_SIZE = 24;
    private static final int DEFAULT_TEXT_GAP = 5;

    /**
     * Private constructor to prevent instantiation.
     */
    private IconUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // --- PUBLIC API METHODS ---

    /**
     * Sets an icon on a Labeled control with default dimensions and standard position (left).
     *
     * @param node The control to set the icon on
     * @param iconName The icon file name (e.g., "home.png" or "home")
     * @throws IllegalArgumentException if node or iconName is null
     */
    public static void setIcon(Labeled node, String iconName) {
        setIcon(node, iconName, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE, null, ContentDisplay.LEFT);
    }

    /**
     * Sets an icon on a Labeled control with custom square dimensions and standard position (left).
     *
     * @param node The control to set the icon on
     * @param iconName The icon file name
     * @param size The desired width and height for the icon
     * @throws IllegalArgumentException if node or iconName is null, or size is negative
     */
    public static void setIcon(Labeled node, String iconName, double size) {
        validateSize(size);
        setIcon(node, iconName, size, size, null, ContentDisplay.LEFT);
    }

    /**
     * Sets an icon on a Labeled control with recoloring and standard position (left).
     *
     * @param node The control to set the icon on
     * @param iconName The icon file name
     * @param color The color to tint the icon with
     * @throws IllegalArgumentException if node, iconName, or color is null
     */
    public static void setIcon(Labeled node, String iconName, Color color) {
        Objects.requireNonNull(color, "Color cannot be null");
        setIcon(node, iconName, DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE, color, ContentDisplay.LEFT);
    }

    /**
     * Sets an icon on a Labeled control with custom dimensions and color, standard position (left).
     *
     * @param node The control to set the icon on
     * @param iconName The icon file name
     * @param width The desired width for the icon
     * @param height The desired height for the icon
     * @param color The color to tint the icon with (null for no coloring)
     * @throws IllegalArgumentException if node or iconName is null, or dimensions are negative
     */
    public static void setIcon(Labeled node, String iconName, double width, double height, Color color) {
        setIcon(node, iconName, width, height, color, ContentDisplay.LEFT);
    }

    /**
     * Sets an icon on a Labeled control with custom dimensions, color, and position.
     * This is the most specific method called by the others.
     *
     * @param node The control to set the icon on
     * @param iconName The icon file name
     * @param width The desired width for the icon
     * @param height The desired height for the icon
     * @param color The color to tint the icon with (null for no coloring)
     * @param contentDisplay The icon position relative to text
     * @throws IllegalArgumentException if required parameters are null or dimensions are negative
     */
    public static void setIcon(Labeled node, String iconName, double width, double height,
                               Color color, ContentDisplay contentDisplay) {
        validateParameters(node, iconName, width, height, contentDisplay);

        createIconView(iconName, width, height, color).ifPresentOrElse(
                iconView -> applyIconToNode(node, iconView, contentDisplay),
                () -> LOGGER.warning("Failed to create icon view for: " + iconName)
        );
    }

    /**
     * Changes the color of an icon in a Labeled control.
     *
     * @param node The control whose icon color to change
     * @param color The new color to apply
     * @throws IllegalArgumentException if node or color is null
     */
    public static void changeIconColor(Labeled node, Color color) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(color, "Color cannot be null");

        if (node.getGraphic() instanceof ImageView imageView) {
            double width = imageView.getFitWidth();
            double height = imageView.getFitHeight();
            applyColorEffect(imageView, width, height, color);
        } else {
            LOGGER.warning("Node does not contain an ImageView graphic");
        }
    }

    /**
     * Changes the color and size of an icon in a Labeled control.
     *
     * @param node The control whose icon to modify
     * @param width The new width for the icon
     * @param height The new height for the icon
     * @param color The new color to apply
     * @throws IllegalArgumentException if node or color is null, or dimensions are negative
     */
    public static void changeIconColorAndSize(Labeled node, double width, double height, Color color) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(color, "Color cannot be null");
        validateSize(width);
        validateSize(height);

        if (node.getGraphic() instanceof ImageView imageView) {
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            applyColorEffect(imageView, width, height, color);
        } else {
            LOGGER.warning("Node does not contain an ImageView graphic");
        }
    }

    /**
     * Changes the color and size of an icon in a Labeled control.
     *
     * @param node The control whose icon to modify
     * @param width The new width for the icon
     * @param height The new height for the icon
     * @throws IllegalArgumentException if node or color is null, or dimensions are negative
     */
    public static void changeIconSize(Labeled node, double width, double height) {
        Objects.requireNonNull(node, "Node cannot be null");
        validateSize(width);
        validateSize(height);

        if (node.getGraphic() instanceof ImageView imageView) {
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        } else {
            LOGGER.warning("Node does not contain an ImageView graphic");
        }
    }


    // --- INTERNAL IMPLEMENTATION ---

    /**
     * Creates an ImageView from an icon file with optional resizing and recoloring.
     *
     * @param iconFileName The icon file name
     * @param width The desired width
     * @param height The desired height
     * @param color The color to apply (null for no coloring)
     * @return An Optional containing the ImageView if successful, empty otherwise
     */
    private static Optional<ImageView> createIconView(String iconFileName, double width, double height, Color color) {
        String fullPath = buildIconPath(iconFileName);

        try (InputStream inputStream = IconUtils.class.getResourceAsStream(fullPath)) {
            if (inputStream == null) {
                LOGGER.log(Level.WARNING, "Icon ''{0}'' not found at path ''{1}''",
                        new Object[]{iconFileName, fullPath});
                return Optional.empty();
            }

            Image icon = new Image(inputStream);
            if (icon.isError()) {
                LOGGER.log(Level.WARNING, "Failed to load image from ''{0}''", fullPath);
                return Optional.empty();
            }

            ImageView iconView = createImageView(icon, width, height);

            if (color != null) {
                applyColorEffect(iconView, width, height, color);
            }

            return Optional.of(iconView);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading icon ''{0}''", iconFileName);
            LOGGER.log(Level.SEVERE, "Exception details", e);
            return Optional.empty();
        }
    }

    /**
     * Creates and configures an ImageView with the specified dimensions.
     */
    private static ImageView createImageView(Image image, double width, double height) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true); // Enable smooth scaling
        return imageView;
    }

    /**
     * Applies the icon to the node with the specified content display.
     */
    private static void applyIconToNode(Labeled node, ImageView iconView, ContentDisplay contentDisplay) {
        node.setGraphic(iconView);
        node.setGraphicTextGap(DEFAULT_TEXT_GAP);
        node.setContentDisplay(contentDisplay);
    }

    /**
     * Applies a color tint effect to the ImageView.
     * Uses BlendMode.SRC_ATOP to color the icon while maintaining original transparency.
     * Works optimally with white or grayscale icons.
     *
     * @param iconView The ImageView to apply the effect to
     * @param width The width for the color input
     * @param height The height for the color input
     * @param color The color to apply
     */
    private static void applyColorEffect(ImageView iconView, double width, double height, Color color) {
        ColorInput colorInput = new ColorInput(0, 0, width, height, color);
        Blend blendEffect = new Blend(BlendMode.SRC_ATOP, null, colorInput);
        iconView.setEffect(blendEffect);
    }

    /**
     * Builds the full path for an icon file.
     */
    private static String buildIconPath(String iconFileName) {
        String adjustedFileName = iconFileName.endsWith(ICON_EXTENSION)
                ? iconFileName
                : iconFileName + ICON_EXTENSION;
        return BASE_PATH + adjustedFileName;
    }

    // --- VALIDATION METHODS ---

    /**
     * Validates the basic parameters for icon setting.
     */
    private static void validateParameters(Labeled node, String iconName, double width, double height,
                                           ContentDisplay contentDisplay) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(iconName, "Icon name cannot be null");
        Objects.requireNonNull(contentDisplay, "Content display cannot be null");

        if (iconName.trim().isEmpty()) {
            throw new IllegalArgumentException("Icon name cannot be empty");
        }

        validateSize(width);
        validateSize(height);
    }

    /**
     * Validates that a size value is positive.
     */
    private static void validateSize(double size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive, got: " + size);
        }
    }
}