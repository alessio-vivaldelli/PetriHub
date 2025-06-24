package com.brunomnsilva.smartgraph.graphview;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShapeSVG implements ShapeWithRadius<SVGPath> {

  public static final double SCALE = 0.04;
  protected final DoubleProperty centerX, centerY;
  protected final DoubleProperty radius;
  protected int markingCount = 1;

  protected final SVGPath svgPath;

  // Fattore per creare uno spazio tra i cerchi. 1.0 = nessun spazio, 0.9 = 10% di
  // spazio.
  private static final double GAP_FACTOR = 0.80;

  public ShapeSVG(double x, double y, double radius, int marking) {
    Args.requireNonNegative(x, "x");
    Args.requireNonNegative(y, "y");
    Args.requireNonNegative(radius, "radius");

    this.svgPath = new SVGPath();
    this.markingCount = marking;

    this.centerX = new SimpleDoubleProperty(x);
    this.centerY = new SimpleDoubleProperty(y);
    this.radius = new SimpleDoubleProperty(radius);

    this.centerX.addListener((observable, oldValue, newValue) -> updateSVG());
    this.centerY.addListener((observable, oldValue, newValue) -> updateSVG());
    this.radius.addListener((observable, oldValue, newValue) -> updateSVG());

    updateSVG();
  }

  protected void updateSVG() {
    double cx = centerX.doubleValue();
    double cy = centerY.doubleValue();
    double outerRadius = getRadius();

    // Raggio fisso per il sistema di coordinate SVG interno, es. 25.
    // Questo semplifica i calcoli di posizionamento.
    double internalRadius = 25.0;

    // Genera i percorsi SVG per i cerchi interni
    String markingsPath = String.join("", generatePackedCircles(internalRadius, markingCount));

    // Costruisce il percorso SVG completo
    String fullPath = markingsPath +
    // Cerchio grande esterno (raggio 25, centrato in 0,0)
        "M 0 -25 " +
        "A 25 25 0 1 0 0 25 " +
        "A 25 25 0 1 0 0 -25 Z";

    svgPath.setContent(fullPath);

    // Scala l'intero SVG per adattarlo al raggio esterno desiderato
    svgPath.setScaleX(outerRadius * SCALE);
    svgPath.setScaleY(outerRadius * SCALE);

    // Posiziona l'SVG nella posizione corretta del pannello
    svgPath.setTranslateX(cx);
    svgPath.setTranslateY(cy);
  }

  /**
   * Genera i percorsi SVG per un numero 'count' di cerchi interni,
   * impacchettati all'interno di un cerchio più grande di raggio R.
   *
   * @param R     Raggio del cerchio contenitore.
   * @param count Numero di cerchi da generare.
   * @return Una lista di stringhe, ognuna rappresentante un percorso SVG per un
   *         cerchio.
   */
  public static List<String> generatePackedCircles(double R, int count) {
    List<String> paths = new ArrayList<>();

    // CASO 1: Nessun cerchio.
    if (count <= 0) {
      return paths;
    }

    // CASO 2: Un singolo cerchio, posizionato al centro.
    if (count == 1) {
      double r = R / 6; // Raggio ragionevole per un cerchio singolo
      paths.add(svgCirclePath(0, 0, r));
      return paths;
    }

    // CASO 3: Cerchi multipli, calcolati con trigonometria.
    // Calcola il raggio massimo possibile per ogni cerchietto in modo che si
    // tocchino appena.
    // La formula deriva da: r <= R / (1/sin(PI/N) + 1)
    double maxRadius = R / (1.5 / Math.sin(Math.PI / count) + 1.0);

    // Applica il fattore di gap per creare spazio.
    double r = maxRadius * GAP_FACTOR;

    // Raggio della circonferenza su cui giacciono i centri dei cerchietti.
    double placementRadius = r / Math.sin(Math.PI / count);

    // Genera i percorsi SVG per ogni cerchio.
    for (int i = 0; i < count; i++) {
      // Calcola l'angolo per distribuire uniformemente i cerchi.
      double angle = 2 * Math.PI * i / count;

      double x = placementRadius * Math.cos(angle);
      double y = placementRadius * Math.sin(angle);
      paths.add(svgCirclePath(x, y, r));
    }

    return paths;
  }

  /**
   * Crea la stringa del percorso SVG per un cerchio.
   */
  private static String svgCirclePath(double x, double y, double radius) {
    // Usiamo Locale.US per garantire che il separatore decimale sia un punto.
    return String.format(Locale.US,
        "M %.2f %.2f " +
            "A %.2f %.2f 0 1 0 %.2f %.2f " +
            "A %.2f %.2f 0 1 0 %.2f %.2f Z",
        x, y - radius,
        radius, radius, x, y + radius,
        radius, radius, x, y - radius);
  }

  @Override
  public Shape getShape() {
    return this.svgPath;
  }

  @Override
  public DoubleProperty centerXProperty() {
    return this.centerX;
  }

  @Override
  public DoubleProperty centerYProperty() {
    return this.centerY;
  }

  @Override
  public DoubleProperty radiusProperty() {
    return this.radius;
  }

  @Override
  public double getRadius() {
    return this.radius.doubleValue();
  }

  @Override
  public void setRadius(double radius) {
    Args.requireNonNegative(radius, "radius");
    this.radius.set(radius);
  }
}
