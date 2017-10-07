
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.CollisionAdapter;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Triangle;
import org.dyn4j.geometry.Vector2;

public class ExampleContactResponse extends JFrame {
    /** The serial version id */
    private static final long serialVersionUID = 5663760293144882635L;

    /** The scale 45 pixels per meter */
    public static final double SCALE = 45.0;

    /** The conversion factor from nano to base */
    public static final double NANO_TO_BASE = 1.0e9;

    public static class StopContactListener extends CollisionAdapter {
        private Body b1, b2;
        public StopContactListener(Body b1, Body b2) {
            this.b1 = b1;
            this.b2 = b2;
        }
        @Override
        public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2, Penetration penetration) {
            // the bodies can appear in either order
            if ((body1 == b1 && body2 == b2) ||
                    (body1 == b2 && body2 == b1)) {
                // its the collision we were looking for
                // do whatever you need to do here

                // stopping them like this isn't really recommended
                // there are probably better ways to do what you want

                body1.getLinearVelocity().zero();
                body1.setAngularVelocity(0.0);
                body2.getLinearVelocity().zero();
                body2.setAngularVelocity(0.0);
                return false;
            }
            return true;
        }
    }

    /**
     * Custom Body class to add drawing functionality.
     * @author William Bittle
     * @version 3.0.2
     * @since 3.0.0
     */
    public static class GameObject extends Body {
        /** The color of the object */
        protected Color color;

        /**
         * Default constructor.
         */
        public GameObject() {
            // randomly generate the color
            this.color = new Color(
                    (float)Math.random() * 0.5f + 0.5f,
                    (float)Math.random() * 0.5f + 0.5f,
                    (float)Math.random() * 0.5f + 0.5f);
        }

        /**
         * Draws the body.
         * <p>
         * Only coded for polygons and circles.
         * @param g the graphics object to render to
         */
        public void render(Graphics2D g) {
            // save the original transform
            AffineTransform ot = g.getTransform();

            // transform the coordinate system from world coordinates to local coordinates
            AffineTransform lt = new AffineTransform();
            lt.translate(this.transform.getTranslationX() * SCALE, this.transform.getTranslationY() * SCALE);
            lt.rotate(this.transform.getRotation());

            // apply the transform
            g.transform(lt);

            // loop over all the body fixtures for this body
            for (BodyFixture fixture : this.fixtures) {
                // get the shape on the fixture
                Convex convex = fixture.getShape();
                // check the shape type
                if (convex instanceof Polygon) {
                    // since Triangle, Rectangle, and Polygon are all of
                    // type Polygon in addition to their main type
                    Polygon p = (Polygon) convex;
                    int l = p.getVertices().length;
                    int[] x = new int[l];
                    int[] y = new int[l];

                    int i = 0;
                    for (Vector2 v : p.getVertices()) {
                        x[i] = (int)(v.x * SCALE);
                        y[i] = (int)(v.y * SCALE);
                        i++;
                    }

                    java.awt.Polygon poly = new java.awt.Polygon(x, y, l);

                    // set the color
                    g.setColor(this.color);
                    // fill the shape
                    g.fillPolygon(poly);
                    // set the color
                    g.setColor(this.color.darker());
                    // draw the shape
                    g.drawPolygon(poly);
                } else if (convex instanceof Circle) {
                    // cast the shape to get the radius
                    Circle c = (Circle) convex;
                    double r = c.getRadius();
                    Vector2 cc = c.getCenter();
                    int x = (int)Math.ceil((cc.x - r) * SCALE);
                    int y = (int)Math.ceil((cc.y - r) * SCALE);
                    int w = (int)Math.ceil(r * 2 * SCALE);
                    // set the color
                    g.setColor(this.color);
                    // fill the shape
                    g.fillOval(x, y, w, w);
                    // set the color
                    g.setColor(this.color.darker());
                    // draw the shape
                    g.drawOval(x, y, w, w);
                }
            }

            // set the original transform
            g.setTransform(ot);
        }
    }

    /** The canvas to draw to */
    protected Canvas canvas;

    /** The dynamics engine */
    protected World world;

    /** Wether the example is stopped or not */
    protected boolean stopped;

    /** The time stamp for the last iteration */
    protected long last;

    /**
     * Default constructor for the window
     */
    public ExampleContactResponse() {
        super("Graphics2D Example");
        // setup the JFrame
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add a window listener
        this.addWindowListener(new WindowAdapter() {
            /* (non-Javadoc)
             * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
             */
            @Override
            public void windowClosing(WindowEvent e) {
                // before we stop the JVM stop the example
                stop();
                super.windowClosing(e);
            }
        });

        // create the size of the window
        Dimension size = new Dimension(800, 600);

        // create a canvas to paint to
        this.canvas = new Canvas();
        this.canvas.setPreferredSize(size);
        this.canvas.setMinimumSize(size);
        this.canvas.setMaximumSize(size);

        // add the canvas to the JFrame
        this.add(this.canvas);

        // make the JFrame not resizable
        // (this way I dont have to worry about resize events)
        this.setResizable(false);

        // size everything
        this.pack();

        // make sure we are not stopped
        this.stopped = false;

        // setup the world
        this.initializeWorld();
    }

    /**
     * Creates game objects and adds them to the world.
     * <p>
     * Basically the same shapes from the Shapes test in
     * the TestBed.
     */
    protected void initializeWorld() {
        // create the world
        this.world = new World();
        this.world.setGravity(World.ZERO_GRAVITY);

        // create all your bodies/joints

        // create the floor
        Rectangle floorRect = new Rectangle(15.0, 1.0);
        GameObject floor = new GameObject();
        floor.addFixture(new BodyFixture(floorRect));
        // move the floor down a bit
        floor.translate(0.0, -4.0);
        this.world.addBody(floor);

        // create a circle
        Circle cirShape = new Circle(0.5);
        GameObject circle = new GameObject();
        circle.addFixture(cirShape);
        circle.translate(2.0, 2.0);
        // test adding some force
        //circle.(new Vector2(-100.0, 0.0));
        circle.setLinearVelocity(new Vector2(-100.0, 0.0));
        // set some linear damping to simulate rolling friction
        circle.setLinearDamping(0.05);
        this.world.addBody(circle);

        // try a rectangle
        Rectangle rectShape = new Rectangle(1.0, 1.0);
        GameObject rectangle = new GameObject();
        rectangle.addFixture(rectShape);
        rectangle.setMass();
        rectangle.translate(0.0, 2.0);
        this.world.addBody(rectangle);

        this.world.addListener(new StopContactListener(circle, rectangle));

    }

    /**
     * Start active rendering the example.
     * <p>
     * This should be called after the JFrame has been shown.
     */
    public void start() {
        // initialize the last update time
        this.last = System.nanoTime();
        // don't allow AWT to paint the canvas since we are
        this.canvas.setIgnoreRepaint(true);
        // enable double buffering (the JFrame has to be
        // visible before this can be done)
        this.canvas.createBufferStrategy(2);
        // run a separate thread to do active rendering
        // because we don't want to do it on the EDT
        Thread thread = new Thread() {
            public void run() {
                // perform an infinite loop stopped
                // render as fast as possible
                while (!isStopped()) {
                    gameLoop();
                    // you could add a Thread.yield(); or
                    // Thread.sleep(long) here to give the
                    // CPU some breathing room
                }
            }
        };
        // set the game loop thread to a daemon thread so that
        // it cannot stop the JVM from exiting
        thread.setDaemon(true);
        // start the game loop
        thread.start();
    }

    /**
     * The method calling the necessary methods to update
     * the game, graphics, and poll for input.
     */
    protected void gameLoop() {
        // get the graphics object to render to
        Graphics2D g = (Graphics2D)this.canvas.getBufferStrategy().getDrawGraphics();

        // before we render everything im going to flip the y axis and move the
        // origin to the center (instead of it being in the top left corner)
        AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
        AffineTransform move = AffineTransform.getTranslateInstance(400, -300);
        g.transform(yFlip);
        g.transform(move);

        // now (0, 0) is in the center of the screen with the positive x axis
        // pointing right and the positive y axis pointing up

        // render anything about the Example (will render the World objects)
        this.render(g);

        // dispose of the graphics object
        g.dispose();

        // blit/flip the buffer
        BufferStrategy strategy = this.canvas.getBufferStrategy();
        if (!strategy.contentsLost()) {
            strategy.show();
        }

        // Sync the display on some systems.
        // (on Linux, this fixes event queue problems)
        Toolkit.getDefaultToolkit().sync();

        // update the World

        // get the current time
        long time = System.nanoTime();
        // get the elapsed time from the last iteration
        long diff = time - this.last;
        // set the last time
        this.last = time;
        // convert from nanoseconds to seconds
        double elapsedTime = (double)diff / NANO_TO_BASE;
        // update the world with the elapsed time
        this.world.update(elapsedTime);
    }

    /**
     * Renders the example.
     * @param g the graphics object to render to
     */
    protected void render(Graphics2D g) {
        // lets draw over everything with a white background
        g.setColor(Color.WHITE);
        g.fillRect(-400, -300, 800, 600);

        // lets move the view up some
        g.translate(0.0, -1.0 * SCALE);

        // draw all the objects in the world
        for (int i = 0; i < this.world.getBodyCount(); i++) {
            // get the object
            GameObject go = (GameObject) this.world.getBody(i);
            // draw the object
            go.render(g);
        }
    }

    /**
     * Stops the example.
     */
    public synchronized void stop() {
        this.stopped = true;
    }

    /**
     * Returns true if the example is stopped.
     * @return boolean true if stopped
     */
    public synchronized boolean isStopped() {
        return this.stopped;
    }

    /**
     * Entry point for the example application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // set the look and feel to the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // create the example JFrame
        ExampleContactResponse window = new ExampleContactResponse();

        // show it
        window.setVisible(true);

        // start it
        window.start();
    }
}
