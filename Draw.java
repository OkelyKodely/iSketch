
import java.awt.AWTException;
import java.awt.AlphaComposite;

import java.awt.Color;

import java.awt.Component;
import java.awt.Dialog;

import java.awt.Dimension;
import java.awt.Font;

import java.awt.Graphics;

import java.awt.Graphics2D;

import java.awt.MouseInfo;

import java.awt.Rectangle;

import java.awt.Robot;
import java.awt.Shape;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.awt.event.MouseEvent;

import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.awt.image.BufferedImage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import javax.swing.JButton;

import javax.swing.JColorChooser;
import javax.swing.JComboBox;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.UIManager;

public class Draw implements MouseListener {

    private int etnTn = 20;
    private int ptnTn = 1;

    private int xx1;
    private int yy1;
    private int xx2;
    private int yy2;

    private BufferedImage bi = null;
    private Graphics2D gf = null;

    private ArrayList<Shape> sl = new ArrayList<Shape>();

    private int xxxx, yyyy;

    private JFrame j = new JFrame("iSketch v.1.8.2");
    private JPanel p = new JPanel();
    private JPanel q = new JPanel();
    private JMenuBar m = new JMenuBar();

    private JMenu file = new JMenu();
    private JMenu edit = new JMenu();
    private JMenu image = new JMenu();
    private JMenu help = new JMenu();

    private String option = "pencil";

    private Color CLR = Color.BLACK;

    public class ColorChooserButton extends JButton {

        private static final long serialVersionUID = 1L;

        private Color current;

        public ColorChooserButton(Color c) {
            setSelectedColor(c);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Color newColor = JColorChooser.showDialog(null, "Choose a color", current);
                    setSelectedColor(newColor);
                    CLR = newColor;
                    q.setBackground(newColor);
                }
            });
        }

        public Color getSelectedColor() {
            return current;
        }

        public void setSelectedColor(Color newColor) {
            setSelectedColor(newColor, true);
            q.setBackground(newColor);
        }

        public void setSelectedColor(Color newColor, boolean notify) {

            if (newColor == null) {
                return;
            }

            current = newColor;
            setIcon(createIcon(current, 16, 16));
            repaint();

            if (notify) {
                // Notify everybody that may be interested.
                for (ColorChangedListener l : listeners) {
                    l.colorChanged(newColor);
                }
            }
        }

        private List<ColorChangedListener> listeners = new ArrayList<ColorChangedListener>();

        public void addColorChangedListener(ColorChangedListener toAdd) {
            listeners.add(toAdd);
        }

        public ImageIcon createIcon(Color main, int width, int height) {
            BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(main);
            graphics.fillRect(0, 0, width, height);
            graphics.setXORMode(Color.DARK_GRAY);
            graphics.drawRect(0, 0, width - 1, height - 1);
            image.flush();
            ImageIcon icon = new ImageIcon(image);
            return icon;
        }
    }

    private class Action {

        int id = -1;

        String action = "pencil";

        int etnTn = 20;

        int ptnTn = 1;

        boolean pencilFirst = false;

        int x = 0;
        int y = 0;
        int x0 = 0;
        int y0 = 0;

        boolean tmp = false;

        Color CLR = Color.BLACK;
    }

    private ColorChooserButton btn = new ColorChooserButton(Color.BLACK);
    public ArrayList<Action> undoList = new ArrayList<Action>();

    public ArrayList<Action> actionList = new ArrayList<Action>();

    private int x, y;
    private int ox, oy;

    private Graphics g;

    volatile private boolean mouseDown = false;

    private boolean off;

    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseDown = true;
            ox = MouseInfo.getPointerInfo().getLocation().x - 200;
            oy = MouseInfo.getPointerInfo().getLocation().y - 50;
            initThread();
            if (e.getClickCount() < 2) {
                if (off) {
                    xx1 = MouseInfo.getPointerInfo().getLocation().x - 200;
                    yy1 = MouseInfo.getPointerInfo().getLocation().y - 50;
                }
            }
            if (option.equals("fill")) {
                for (int i = 0; i < sl.size(); i++) {
                    Shape pp = sl.get(i);
                    if (pp.contains(ox, oy)) {
                        Graphics2D g2 = (Graphics2D) p.getGraphics();
                        g2.setColor(CLR);
                        g2.fill(pp);
                        Action action = new Action();
                        action.action = "fill";
                        action.CLR = CLR;
                        action.x = i;
                        addAction(action);
                    }
                }
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (e.getClickCount() > 1) {
                xxxx = MouseInfo.getPointerInfo().getLocation().x - 200;
                yyyy = MouseInfo.getPointerInfo().getLocation().y - 50;

                BufferedImage b = new BufferedImage(Math.abs(xx2 - xx1), Math.abs(yy2 - yy1), BufferedImage.TYPE_INT_RGB);

                Graphics2D g2d = b.createGraphics();
                g2d.setComposite(
                        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

                g = p.getGraphics();
                g.drawImage(bi, xxxx, yyyy, xxxx + Math.abs(xx2 - xx1), yyyy + Math.abs(yy2 - yy1), xx1, yy1, xx2, yy2, p);
            }
        }
    }

    public void mouseReleased(MouseEvent e) {

        //if(option.equals("pencil"))
        ft = true;

        if (option.equals("copy") && e.getButton() == MouseEvent.BUTTON1) {
            xx2 = MouseInfo.getPointerInfo().getLocation().x - 200;
            yy2 = MouseInfo.getPointerInfo().getLocation().y - 50;
            drawSelecting();
            off = false;
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            off = true;
            mouseDown = false;
            if (option.equals("oval")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "oval";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = false;
                addAction(action);
                doAction(action);
            }
            if (option.equals("line")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "line";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = false;
                addAction(action);
                doAction(action);
            } else if (option.equals("drect")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "drect";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                Shape s = new Shape() {
                    @Override
                    public Rectangle getBounds() {
                        return null;
                    }

                    @Override
                    public Rectangle2D getBounds2D() {
                        return null;
                    }

                    @Override
                    public boolean contains(double x, double y) {
                        return false;
                    }

                    @Override
                    public boolean contains(Point2D p) {
                        return false;
                    }

                    @Override
                    public boolean intersects(double x, double y, double w, double h) {
                        return false;
                    }

                    @Override
                    public boolean intersects(Rectangle2D r) {
                        return false;
                    }

                    @Override
                    public boolean contains(double x, double y, double w, double h) {
                        if (x == action.x0 && y == action.y0
                                && Math.abs(action.x0 - action.x) == w
                                && Math.abs(action.y0 - action.y) == h) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    public boolean contains(Rectangle2D r) {
                        return false;
                    }

                    public PathIterator getPathIterator(AffineTransform at) {
                        return null;
                    }

                    public PathIterator getPathIterator(AffineTransform at, double flatness) {
                        return null;
                    }
                };
                Shape s1 = new Rectangle(action.x0, action.y0, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
                sl.add(s1);
                action.CLR = CLR;
                action.tmp = false;
                addAction(action);
                doAction(action);
            } else if (option.equals("frect")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "frect";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = false;
                addAction(action);
                doAction(action);
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void undrawSelecting() {
        try {
            g = p.getGraphics();

            g.setColor(Color.white);

            g.drawLine(xx1, yy1, xx2, yy1);
            g.drawLine(xx1, yy2, xx2, yy2);

            g.drawLine(xx1, yy1, xx1, yy2);
            g.drawLine(xx2, yy1, xx2, yy2);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    public void drawSelecting() {
        try {
            g = p.getGraphics();

            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(xx1, yy1, xx2, yy1);
            g.setColor(Color.GRAY);
            g.drawLine(xx1, yy2, xx2, yy2);

            g.setColor(Color.BLUE);
            g.drawLine(xx1, yy1, xx1, yy2);
            g.setColor(Color.RED);
            g.drawLine(xx2, yy1, xx2, yy2);

            Dimension d = p.getPreferredSize();
            int w = (int) d.getWidth();
            int h = (int) d.getHeight();

            bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

            gf = bi.createGraphics();

            p.paint(gf);

            gf.dispose();

        } catch (IllegalStateException is) {
            is.printStackTrace();
        }
    }

    private boolean isRunning = false;

    private boolean checkAndMark() {
        if (isRunning) {
            return false;
        }
        isRunning = true;
        return true;
    }

    Thread tt = new Thread() {
        public void run() {
            do {
                if (isRunning) {
                    mousMoved(null);
                }
                if (!mouseDown) {
                    ft = true;
                    isRunning = false;
                }
            } while (true);
        }
    };

    boolean ttf = true;

    private void initThread() {
        if (checkAndMark()) {
            if (ttf) {
                g = p.getGraphics();
                tt.start();
                ttf = false;
            }
        }
    }

    public void clearScreen() {
        g = p.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 1200, 750);
    }

    public void mousMoved(MouseEvent arg0) {
        try {
            if (option.equals("line")) {
                clearScreen();
                doActionList();
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "line";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = true;
                //doAction(action);
            } else if (option.equals("drect")) {
                clearScreen();
                doActionList();
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "drect";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = true;
                //doAction(action);
            } else if (option.equals("frect")) {
                clearScreen();
                doActionList();
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "frect";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = true;
                //doAction(action);
            } else if (option.equals("pencil")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "pencil";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = ox;
                //if(ft == true)
                action.ptnTn = ptnTn;
                action.pencilFirst = false;
                action.CLR = CLR;
                doAction(action);
                addAction(action);
            } else if (option.equals("erase")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "erase";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.etnTn = etnTn;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = false;
                addAction(action);
                doAction(action);
            } else if (option.equals("spray")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "spray";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = ox;
                //if(ft == true)
                action.ptnTn = ptnTn;
                action.pencilFirst = false;
                action.CLR = CLR;
                doAction(action);
                addAction(action);
            }
        } catch (Exception exception) {
        }
    }

    public void addAction(Action action) {
        actionList.add(action);
    }

    private void doActionList() {
        try {
            int index = 0;
            while (index < actionList.size()) {
                if (!actionList.get(index).tmp) {
                    if (actionList.get(index).action.equals("pencil")
                            && actionList.get(index).pencilFirst == true) {
                        ft = true;
                    }
                    doAction(actionList.get(index));
                }
                index++;
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    boolean ft = true;

    public void doAction(Action action) {
        if (action.action.equals("oval")) {
            g.setColor(action.CLR);
            g.drawOval(action.x0, action.y0, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
        } else if (action.action.equals("fill")) {
            Shape pp = sl.get(action.x);
            Graphics2D g2 = (Graphics2D) p.getGraphics();
            g2.setColor(action.CLR);
            g2.fill(pp);
        } else if (action.action.equals("line")) {
            g.setColor(action.CLR);
            g.drawLine(action.x0, action.y0, action.x, action.y);
        } else if (action.action.equals("spray")) {
            g.setColor(action.CLR);
            for (int i = 0; i < 1; i++) {
                Random rd = new Random();
                int x = rd.nextInt(20);
                int y = rd.nextInt(20);
                int x1 = rd.nextInt(18) - rd.nextInt(18);
                int y1 = rd.nextInt(18) - rd.nextInt(18);
                g.drawOval(action.x + x1, action.y + y1, x, y);
            }
        } else if (action.action.equals("pencil")) {
            g.setColor(action.CLR);
            if (!action.pencilFirst) {
                Action a = null;
                for (int i = actionList.size() - 1; i >= 0; i--) {
                    if (i == action.id - 1 && !ft) {
                        a = actionList.get(i);
                    }
                }
                try {
                    if (action.ptnTn == 1) {
                        g.drawLine(a.x, a.y, action.x, action.y);
                    }
                    if (action.ptnTn == 2) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                    }
                    if (action.ptnTn == 3) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
//                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                    }
                    if (action.ptnTn == 4) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
//                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
//                                    g.drawLine(a.x+3, a.y, action.x+3, action.y);                                
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                        g.drawLine(a.x, a.y + 3, action.x, action.y + 3);
                    }
                    if (action.ptnTn == 5) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
//                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
//                                    g.drawLine(a.x+3, a.y, action.x+3, action.y);                                
//                                    g.drawLine(a.x+4, a.y, action.x+4, action.y);                                
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                        g.drawLine(a.x, a.y + 3, action.x, action.y + 3);
                        g.drawLine(a.x, a.y + 4, action.x, action.y + 4);
                    }
                    if (action.ptnTn == 10) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
//                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
//                                    g.drawLine(a.x+3, a.y, action.x+3, action.y);                                
//                                    g.drawLine(a.x+4, a.y, action.x+4, action.y);                                
//                                    g.drawLine(a.x+5, a.y, action.x+5, action.y);                                
//                                    g.drawLine(a.x+6, a.y, action.x+6, action.y);                                
//                                    g.drawLine(a.x+7, a.y, action.x+7, action.y);                                
//                                    g.drawLine(a.x+8, a.y, action.x+8, action.y);                                
//                                    g.drawLine(a.x+9, a.y, action.x+9, action.y);                                
//                                    
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                        g.drawLine(a.x, a.y + 3, action.x, action.y + 3);
                        g.drawLine(a.x, a.y + 4, action.x, action.y + 4);
                        g.drawLine(a.x, a.y + 5, action.x, action.y + 5);
                        g.drawLine(a.x, a.y + 6, action.x, action.y + 6);
                        g.drawLine(a.x, a.y + 7, action.x, action.y + 7);
                        g.drawLine(a.x, a.y + 8, action.x, action.y + 8);
                        g.drawLine(a.x, a.y + 9, action.x, action.y + 9);
                    }
                    if (action.ptnTn == 20) {
                        g.drawLine(a.x, a.y, action.x, action.y);
                        for (int i = 1; i < 20; i++) {
                            //g.drawLine(a.x+i, a.y, action.x+i, action.y);                                
                            g.drawLine(a.x, a.y + i, action.x, action.y + i);
                        }
                    }
                    if (action.ptnTn == 50) {
                        g.drawLine(a.x, a.y, action.x, action.y);
                        for (int i = 1; i < 50; i++) {
                            //g.drawLine(a.x+i, a.y, action.x+i, action.y);                                
                            g.drawLine(a.x, a.y + i, action.x, action.y + i);
                        }
                    }
                    if (action.ptnTn == 100) {
                        g.drawLine(a.x, a.y, action.x, action.y);
                        for (int i = 1; i < 100; i++) {
                            //g.drawLine(a.x-i, a.y, action.x-i, action.y);                                
                            g.drawLine(a.x, a.y + i, action.x, action.y + i);
                            //g.drawLine(a.x-i, a.y+i, action.x-i, action.y+i);                                
                        }
                    }

                } catch (Exception e) {
                    g.drawLine(action.x, action.y, action.x - 1, action.y);
                    ft = false;
                }
            } else {
                g.drawLine(action.x, action.y, action.x - 1, action.y);
                ft = false;
            }
        } else if (action.action.equals("erase")) {
            g.setColor(Color.WHITE);
            if (!action.pencilFirst) {
                Action a = null;
                for (int i = actionList.size() - 1; i >= 0; i--) {
                    if (i == action.id - 1 && !ft) {
                        a = actionList.get(i);
                    }
                }
                try {
                    if (action.etnTn == 1) {
                        g.drawLine(a.x, a.y, action.x, action.y);
                    }
                    if (action.etnTn == 2) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                    }
                    if (action.etnTn == 3) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
//                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                    }
                    if (action.etnTn == 4) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
//                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
//                                    g.drawLine(a.x+3, a.y, action.x+3, action.y);                                
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                        g.drawLine(a.x, a.y + 3, action.x, action.y + 3);
                    }
                    if (action.etnTn == 5) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
//                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
//                                    g.drawLine(a.x+3, a.y, action.x+3, action.y);                                
//                                    g.drawLine(a.x+4, a.y, action.x+4, action.y);                                
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                        g.drawLine(a.x, a.y + 3, action.x, action.y + 3);
                        g.drawLine(a.x, a.y + 4, action.x, action.y + 4);
                    }
                    if (action.etnTn == 10) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
//                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
//                                    g.drawLine(a.x+3, a.y, action.x+3, action.y);                                
//                                    g.drawLine(a.x+4, a.y, action.x+4, action.y);                                
//                                    g.drawLine(a.x+5, a.y, action.x+5, action.y);                                
//                                    g.drawLine(a.x+6, a.y, action.x+6, action.y);                                
//                                    g.drawLine(a.x+7, a.y, action.x+7, action.y);                                
//                                    g.drawLine(a.x+8, a.y, action.x+8, action.y);                                
//                                    g.drawLine(a.x+9, a.y, action.x+9, action.y);                                
//                                    
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                        g.drawLine(a.x, a.y + 3, action.x, action.y + 3);
                        g.drawLine(a.x, a.y + 4, action.x, action.y + 4);
                        g.drawLine(a.x, a.y + 5, action.x, action.y + 5);
                        g.drawLine(a.x, a.y + 6, action.x, action.y + 6);
                        g.drawLine(a.x, a.y + 7, action.x, action.y + 7);
                        g.drawLine(a.x, a.y + 8, action.x, action.y + 8);
                        g.drawLine(a.x, a.y + 9, action.x, action.y + 9);
                    }
                    if (action.etnTn == 20) {
                        g.drawLine(a.x, a.y, action.x, action.y);
                        for (int i = 1; i < 20; i++) {
                            //g.drawLine(a.x+i, a.y, action.x+i, action.y);                                
                            g.drawLine(a.x, a.y + i, action.x, action.y + i);
                        }
                    }
                    if (action.etnTn == 50) {
                        g.drawLine(a.x, a.y, action.x, action.y);
                        for (int i = 1; i < 50; i++) {
                            //g.drawLine(a.x+i, a.y, action.x+i, action.y);                                
                            g.drawLine(a.x, a.y + i, action.x, action.y + i);
                        }
                    }
                    if (action.etnTn == 100) {
                        g.drawLine(a.x, a.y, action.x, action.y);
                        for (int i = 1; i < 100; i++) {
                            //g.drawLine(a.x-i, a.y, action.x-i, action.y);                                
                            g.drawLine(a.x, a.y + i, action.x, action.y + i);
                            //g.drawLine(a.x-i, a.y+i, action.x-i, action.y+i);                                
                        }
                    }
                } catch (Exception e) {
                    g.drawLine(action.x, action.y, action.x - 1, action.y);
                    ft = false;
                }
            } else {
                g.drawLine(action.x, action.y, action.x - 1, action.y);
                ft = false;
            }
        } else if (action.action.equals("drect")) {
            g.setColor(action.CLR);
            if (action.x0 < action.x && action.y0 < action.y) {
                g.drawRect(action.x0, action.y0, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
            if (action.x0 > action.x && action.y0 < action.y) {
                g.drawRect(action.x, action.y0, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
            if (action.x0 < action.x && action.y0 > action.y) {
                g.drawRect(action.x0, action.y, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
            if (action.x0 > action.x && action.y0 > action.y) {
                g.drawRect(action.x, action.y, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
        } else if (action.action.equals("frect")) {
            g.setColor(action.CLR);
            if (action.x0 < action.x && action.y0 < action.y) {
                g.fillRect(action.x0, action.y0, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
            if (action.x0 > action.x && action.y0 < action.y) {
                g.fillRect(action.x, action.y0, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
            if (action.x0 < action.x && action.y0 > action.y) {
                g.fillRect(action.x0, action.y, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
            if (action.x0 > action.x && action.y0 > action.y) {
                g.fillRect(action.x, action.y, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
        }
    }

    public Draw() {
        Action actio = new Action();
        actio.id = 0;
        actionList.add(actio);
        j.setLayout(null);
        j.setBounds(0, 0, 1200, 750);
        p.setBounds(200, 0, 1000, 750);
        p.setBackground(Color.white);
        j.add(p);
        p.addMouseListener(this);
        j.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        JMenuItem ab = new JMenuItem();

        ab.setText("about");

        help.add(ab);

        ab.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                final JDialog d = new JDialog(j);
                d.setBounds(0, 0, 500, 500);
                JLabel l = new JLabel("by DHC");
                l.setBounds(0, 0, 50, 20);
                d.add(l);
                d.setLayout(null);
                JButton b = new JButton("Close");
                d.add(b);
                b.setBounds(100, 100, 75, 60);
                b.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {

                        d.dispose();
                    }
                });
                d.setVisible(true);
            }
        });

        JMenuItem mi5 = new JMenuItem();

        mi5.setText("undo");

        mi5.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                clearScreen();
                if (actionList.get(actionList.size() - 1).action.equals("pencil")) {
                    boolean isPencilStill = true;
                    for (int i = actionList.size() - 1; i >= 0 && isPencilStill; i--) {
                        if (actionList.get(actionList.size() - 1).action.equals("pencil")) {
                            undoList.add(actionList.get(i));
                            actionList.remove(actionList.get(i));
                            actionList.trimToSize();
                        } else {
                            isPencilStill = false;
                        }
                        if (isPencilStill == false) {
                            break;
                        }
                    }
                } else {
                    undoList.add(actionList.get(actionList.size() - 1));
                    actionList.remove(actionList.size() - 1);
                    actionList.trimToSize();
                }
                doActionList();
            }
        });

        JMenuItem mi6 = new JMenuItem();

        mi6.setText("redo");

        mi6.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                clearScreen();
                try {
                    if (undoList.get(undoList.size() - 1).action.equals("pencil")) {
                        boolean isPencilStill = true;
                        for (int i = undoList.size() - 1; i > 0 && isPencilStill; i--) {
                            if (undoList.get(undoList.size() - 1).action.equals("pencil")) {
                                actionList.add(undoList.get(i));
                                undoList.remove(undoList.get(i));
                            }
                        }
                    } else {
                        actionList.add(undoList.get(undoList.size() - 1));
                        undoList.remove(undoList.get(undoList.size() - 1));
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
                undoList.trimToSize();
                doActionList();
            }
        });

        edit.add(mi5);
        edit.add(mi6);

        JMenuItem mi1 = new JMenuItem();

        mi1.setText("save");

        JMenuItem mi3 = new JMenuItem();

        JMenuItem mi7 = new JMenuItem();

        mi7.setText("use eraser");

        mi7.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option = "erase";
            }
        });

        image.add(mi7);

        mi3.setText("use pencil");

        mi3.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option = "pencil";
            }
        });

        JMenuItem mi2 = new JMenuItem();

        JMenuItem mi9 = new JMenuItem();

        mi9.setText("fill rectangle");

        mi9.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option = "frect";
            }
        });

        image.add(mi9);

        JMenuItem mi8 = new JMenuItem();

        mi8.setText("draw rectangle");

        mi8.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option = "drect";
            }
        });

        image.add(mi8);

        mi2.setText("end");

        m.add(file);

        m.add(edit);

        JMenuItem black = new JMenuItem();

        black.setText("BLACK");

        black.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CLR = Color.BLACK;
                btn.setSelectedColor(CLR);
            }
        });

        JMenuItem org = new JMenuItem();

        org.setText("ORANGE");

        org.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CLR = Color.ORANGE;
                btn.setSelectedColor(CLR);
            }
        });

        JMenuItem re = new JMenuItem();

        re.setText("RED");

        re.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CLR = Color.RED;
                btn.setSelectedColor(CLR);
            }
        });

        edit.add(re);

        JMenuItem ye = new JMenuItem();

        ye.setText("YELLOW");

        ye.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CLR = Color.YELLOW;
                btn.setSelectedColor(CLR);
            }
        });

        edit.add(ye);

        JMenuItem gr = new JMenuItem();

        gr.setText("GRAY");

        gr.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CLR = Color.GRAY;
                btn.setSelectedColor(CLR);
            }
        });

        edit.add(gr);

        JMenuItem bl = new JMenuItem();

        bl.setText("BLUE");

        bl.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CLR = Color.BLUE;
                btn.setSelectedColor(CLR);
            }
        });

        edit.add(bl);
        edit.add(org);
        edit.add(black);

        m.add(image);
        m.add(help);

        edit.setText("Edit");
        image.setText("Tool");
        help.setText("Help");

        JMenuItem mi4 = new JMenuItem();
        mi4.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option = "line";
            }
        });

        mi4.setText("draw line");

        JMenuItem mii = new JMenuItem();
        mii.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option = "copy";
            }
        });

        mii.setText("copy");

        image.add(mii);
        image.add(mi3);
        image.add(mi4);

        mi2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });

        mi1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser();
                int returnVal = chooser.showSaveDialog(j);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        Robot robot = new Robot();
                        String format = "jpg";
                        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                        BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
                        ImageIO.write(screenFullImage, format, new File(chooser.getSelectedFile().getAbsolutePath()));

                        System.out.println("A full screenshot saved!");
                    } catch (Exception ex) {
                        System.err.println(ex);
                    }
                    JOptionPane.showMessageDialog(null, "saved!");
                }
            }
        });

        file.add(mi2);
        file.setText("File");

        q.setBounds(0, 0, 192, 750);
        q.setBackground(Color.white);
        j.add(q);
        JButton pnc = new JButton("brush");
        pnc.setBounds(0, 0, 100, 35);
        JButton lne = new JButton("line");
        lne.setBounds(100, 0, 100, 35);
        JButton drect = new JButton("rect");
        drect.setBounds(0, 35, 100, 35);
        JButton frect = new JButton("fill rect");
        frect.setBounds(100, 35, 100, 35);

        q.add(pnc);
        q.add(lne);
        q.add(drect);
        q.add(frect);

        q.add(btn);

        JButton wh = new JButton("white");
        wh.setBounds(0, 370, 100, 30);
        JButton yell = new JButton("yellow");
        yell.setBounds(0, 70, 100, 30);
        yell.setBackground(Color.yellow);
        JButton orag = new JButton("orange");
        orag.setBounds(0, 100, 100, 30);
        orag.setBackground(Color.orange);
        JButton pnk = new JButton("pink");
        pnk.setBounds(0, 130, 100, 30);
        pnk.setBackground(Color.pink);
        JButton ble = new JButton("blue");
        ble.setBounds(0, 160, 100, 30);
        ble.setBackground(Color.blue);
        JButton green = new JButton("green");
        green.setBackground(Color.GREEN);
        green.setBounds(0, 190, 100, 30);
        JButton rd = new JButton("red");
        rd.setBounds(0, 220, 100, 30);
        rd.setBackground(Color.red);
        JButton grey = new JButton("grey");
        grey.setBounds(0, 250, 100, 30);
        grey.setBackground(Color.GRAY);
        JButton balk = new JButton("black");
        balk.setBounds(0, 280, 100, 30);
        balk.setBackground(Color.BLACK);
        balk.setForeground(Color.WHITE);
        JButton cyn = new JButton("cyan");
        cyn.setBackground(Color.CYAN);
        cyn.setBounds(0, 310, 100, 30);
        JButton era = new JButton("erase");
        era.setBounds(0, 340, 100, 30);
        JComboBox etn = new JComboBox();
        q.add(etn);
        etn.addItem("erase thickness");
        etn.addItem("1");
        etn.addItem("2");
        etn.addItem("3");
        etn.addItem("4");
        etn.addItem("5");
        etn.addItem("10");
        etn.addItem("20");
        etn.addItem("50");
        etn.addItem("100");
        etn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int v = Integer.parseInt(((JComboBox) e.getSource()).getSelectedItem().toString());
                etnTn = v;
            }
        });
        JButton fill = new JButton("fill");
        fill.setBackground(Color.magenta);
        JComboBox ptn = new JComboBox();
        q.add(ptn);
        ptn.addItem("brush thickness");
        ptn.addItem("1");
        ptn.addItem("2");
        ptn.addItem("3");
        ptn.addItem("4");
        ptn.addItem("5");
        ptn.addItem("10");
        ptn.addItem("20");
        ptn.addItem("50");
        ptn.addItem("100");
        ptn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int v = Integer.parseInt(((JComboBox) e.getSource()).getSelectedItem().toString());
                ptnTn = v;
            }
        });

        pnc.setBackground(Color.magenta);
        lne.setBackground(Color.magenta);
        drect.setBackground(Color.magenta);
        frect.setBackground(Color.magenta);
        era.setBackground(Color.magenta);

        JButton spray = new JButton("spray");
        spray.setBackground(Color.magenta);
        spray.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                option = "spray";
            }
        });

        JButton fit = new JButton("fit to screen");
        fit.setBackground(Color.magenta);
        fit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clearScreen();
                    Graphics2D g2 = (Graphics2D) p.getGraphics();
                    g2.drawImage(image1, 0, 0, p.getWidth(), p.getHeight(), null);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        JButton og = new JButton("original size");
        og.setBackground(Color.magenta);
        og.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clearScreen();
                    Graphics2D g2 = (Graphics2D) p.getGraphics();
                    g2.drawImage(image1, 0, 0, null);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        btn.setBackground(Color.DARK_GRAY);

        q.add(spray);
        q.add(fit);
        q.add(og);
        q.add(fill);
        q.add(yell);
        q.add(orag);
        q.add(pnk);
        q.add(ble);
        q.add(green);
        q.add(rd);
        q.add(grey);
        q.add(balk);
        q.add(cyn);
        q.add(era);
        q.add(wh);

        pnc.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                option = "pencil";
            }
        });
        fill.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                option = "fill";
            }
        });
        wh.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.white;
                btn.setSelectedColor(CLR);
            }
        });
        lne.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                option = "line";
            }
        });
        drect.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                option = "drect";
            }
        });
        frect.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                option = "frect";
            }
        });

        yell.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.YELLOW;
                btn.setSelectedColor(CLR);
            }
        });

        grey.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.GRAY;
                btn.setSelectedColor(CLR);
            }
        });

        orag.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.ORANGE;
                btn.setSelectedColor(CLR);
            }
        });

        pnk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.PINK;
                btn.setSelectedColor(CLR);
            }
        });

        rd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.RED;
                btn.setSelectedColor(CLR);
            }
        });

        ble.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.blue;
                btn.setSelectedColor(CLR);
            }
        });

        balk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.BLACK;
                btn.setSelectedColor(CLR);
            }
        });

        cyn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.CYAN;
                btn.setSelectedColor(CLR);
            }
        });

        green.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.green;
                btn.setSelectedColor(CLR);
            }
        });

        era.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                option = "erase";
            }
        });

        JButton oval = new JButton("oval");
        oval.setBackground(Color.magenta);
        q.add(oval);
        oval.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                option = "oval";
            }
        });

        JMenuItem save = new JMenuItem("save");
        file.add(save);
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Rectangle screenRect = new Rectangle(200, 44, j.getWidth() - 200, j.getHeight() - 44);
                    BufferedImage capture = new Robot().createScreenCapture(screenRect);
                    ImageIO.write(capture, "jpg", new File("imagetemp.jpg"));
                    JFileChooser fc = new JFileChooser();
                    int returnVal = fc.showSaveDialog(p);
                    Thread t = new Thread() {
                        public void run() {
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                File file = new File(fc.getSelectedFile().getAbsolutePath());
                                File f = new File("imagetemp.jpg");
                                f.renameTo(file);
                            }
                        }
                    };
                    t.start();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (AWTException awte) {
                    awte.printStackTrace();
                }
            }
        });

        JMenuItem open = new JMenuItem("open");
        file.add(open);
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(p);
                Thread t = new Thread() {
                    public void run() {
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = new File(fc.getSelectedFile().getAbsolutePath());
                            if (!file.getAbsolutePath().endsWith(".jpg")
                                    && !file.getAbsolutePath().endsWith(".gif")) {
                                JOptionPane.showMessageDialog(null, "Not a jpg or gif");
                            } else {
                                try {
                                    ///Thread.sleep(2000);
                                    image1 = ImageIO.read(file);
                                    Graphics2D g2 = (Graphics2D) p.getGraphics();
                                    g2.drawImage(image1, 0, 0, p.getWidth(), p.getHeight(), p);
                                } catch (Exception ex) {
                                    System.out.println("ERROR");
                                }
                            }
                        }
                    }
                };
                t.start();
            }
        });

        JMenuItem neww = new JMenuItem("clear screen");
        file.add(neww);
        neww.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                p.setBackground(Color.WHITE);
                clearScreen();
            }
        });

        JButton main = new JButton("starting up ");
        p.add(main);
        p.setLayout(null);
        p.setBackground(Color.black);
        main.setFont(new Font("arial", Font.BOLD, 70));
        main.setBounds(0, 0, j.getWidth(), j.getHeight());
        main.setBackground(Color.orange);
        main.setForeground(Color.WHITE);
        CLR = Color.BLUE;
        btn.setSelectedColor(CLR);
        main.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (f) {
                    p.remove(main);
                    p.setBackground(Color.WHITE);
                    clearScreen();
                }
            }
        });
        Thread t1 = new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                        main.setText(main.getText() + ".");
                        if (main.getText().length() > 90) {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                f = true;
                main.setText("click.");
                main.setBackground(Color.green);
            }
        };
        t1.start();

        JLabel title = new JLabel("iSketch");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("arial", Font.BOLD, 45));
        q.add(title);
        JLabel title2 = new JLabel("v.1.8.2");
        title2.setForeground(Color.WHITE);
        title2.setFont(new Font("arial", Font.BOLD, 25));
        q.add(title2);

        j.setJMenuBar(m);
        j.setExtendedState(JFrame.MAXIMIZED_BOTH);
        j.setVisible(true);
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        Draw draw = new Draw();
        draw.q.setBackground(Color.BLUE);
        Dimension DimMax = Toolkit.getDefaultToolkit().getScreenSize();
        draw.j.setExtendedState(JFrame.MAXIMIZED_BOTH);
        draw.j.setMaximumSize(DimMax);
        draw.j.setBounds(0, 0, (int) DimMax.getWidth(), (int) DimMax.getHeight());
        draw.p.setBounds(200, 0, DimMax.width - 200, 750);
        draw.j.setResizable(false);
    }
    boolean f = false;
    BufferedImage image1 = null;
}
