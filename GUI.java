import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class GUI extends JFrame {
    private static final long serialVersionUID = 1L;

    /* euclidean or manhatten distance */
    private final int EUCLIDEAN = 0;
    private final int MANHATTEN = 1;
    private int mode = EUCLIDEAN;

    /* points and their color */
    private final int num_points = 20;
    private Point2D.Double points[];
    private Color colors[];

    /* drawing and dragging */
    private final int draw_radius = 4;
    private final int drag_radius = 6;
    private Point2D.Double over   = null;

    /* double buffering */
    private BufferedImage buf;

    public GUI() {
        setBounds(0, 0, 800, 600);

        double w = getWidth();
        double h = getHeight();

        points = new Point2D.Double[num_points];
        colors = new Color[num_points];

        /* allocate points and colors */
        for (int i = 0; i < num_points; i++) {
            int r, g, b;
            boolean duplicate = false;

            points[i] = new Point2D.Double(w / 4 + Math.random() * (w / 2), h / 4 + Math.random() * (h / 2));

            /* random colors */
            do {
                r = (int) (Math.random() * 256);
                g = (int) (Math.random() * 256);
                b = (int) (Math.random() * 256);

                for (int j = 0; j < i; j++) {
                    duplicate = false;

                    if (colors[j].getRed() == r && colors[j].getGreen() == g && colors[j].getBlue() == b)
                        duplicate = true;
                }
            } while(duplicate);

            colors[i] = new Color(r, g, b);
        }

        buf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight());

        /* for the future: also resize buffer on resize event */
        addComponentListener(new ComponentListener() {
            public void componentShown(ComponentEvent e) { }
            public void componentResized(ComponentEvent e) {
                buf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight());
            }
            public void componentMoved(ComponentEvent e) { }
            public void componentHidden(ComponentEvent e) { }
        });

        addMouseListener(new MyMouseListener());
        addMouseMotionListener(new MyMouseMotionListener());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Voronoi");
        setResizable(false);
        setVisible(true);
    }

    public void paint(Graphics out) {
        Graphics g = buf.getGraphics();
        int w = buf.getWidth();
        int h = buf.getHeight();

        /* calculate distance of every pixel to each point */
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double dist = Integer.MAX_VALUE;
                int min     = -1;

                for (int i = 0; i < num_points; i++) {
                    double d = Integer.MAX_VALUE;

                    switch (mode) {
                        case EUCLIDEAN:
                            d = points[i].distance(x, y);
                            break;

                        case MANHATTEN:
                            d = Math.abs(points[i].x - x) + Math.abs(points[i].y - y);
                            break;
                    }

                    if (d > dist)
                        continue;

                    dist = d;
                    min  = i;
                }

                if (min == -1) {
                    System.err.println("Bug: out of bounce");
                    continue;
                }

                buf.setRGB(x, y, colors[min].getRGB());
            }
        }

        /* draw points */
        g.setColor(Color.black);

        for (Point2D.Double i: points) {
            if (i == over) {
                g.setColor(Color.red);
                g.fillArc((int) i.x - drag_radius, (int) i.y - drag_radius, 2 * drag_radius, 2 * drag_radius, 0, 360);				
                g.setColor(Color.black);
                continue;
            }

            g.fillArc((int) i.x - draw_radius, (int) i.y - draw_radius, 2 * draw_radius, 2 * draw_radius, 0, 360);
        }

        /* flip buffer */
        out.drawImage(buf, 0, 0, getWidth(), getHeight(), this);
    }

    class MyMouseListener implements MouseListener {
        public void mouseClicked(MouseEvent e) {
            /* right click -> change distance mode */
            if (SwingUtilities.isRightMouseButton(e)) {
                if (mode == EUCLIDEAN)
                    mode = MANHATTEN;
                else
                    mode = EUCLIDEAN;

                repaint();
            }
        }
        public void mousePressed(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) {	}		
    }

    class MyMouseMotionListener implements MouseMotionListener {
        /* drag point to different position */
        public void mouseDragged(MouseEvent e) {
            if (over == null)
                return;

            over.setLocation(e.getX(), e.getY());
            repaint();
        }

        /* check if mouse is over a point */
        public void mouseMoved(MouseEvent e) {
            Point2D.Double mouse = new Point2D.Double(e.getX(), e.getY());

            for (Point2D.Double i : points) {
                if (mouse.distance(i) > drag_radius)
                    continue;

                over = i;
                repaint();
                return;
            }

            if (over == null)
                return;

            over = null;
            repaint();
        }
    }
}
