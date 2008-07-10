/*
 * Caricature.java
 *
 * Created on April 8, 2006, 4:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package swing.binding.caricature;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author sky
 */
public class JCaricature extends JPanel {
    private static Map/*<String,Image>*/ imageMap;
    
    private boolean empty;
    private int mouthStyle;
    private int faceStyle;
    private int hairStyle;
    private int eyeStyle;
    private int noseStyle;
    private int rotation;
    private float scale = 1.0f;
    
    public JCaricature() {
        if (imageMap == null) {
            imageMap = new HashMap/*<String,Image>*/(1);
            for (int i = 0; i < 5; i++) {
                getImage("face", i);
                getImage("hair", i);
                getImage("eyes", i);
                getImage("nose", i);
                getImage("mouth", i);
            }
        }
    }
    
    public void setEmpty(boolean empty) {
        if (this.empty != empty) {
            this.empty = empty;
            firePropertyChange("empty", !empty, empty);
            repaint();
        }
    }
    
    public boolean isEmpty() {
        return empty;
    }
    
    public void setRotation(int rotation) {
        int oldRotation = this.rotation;
        this.rotation = rotation;
        repaint();
        firePropertyChange("rotation", oldRotation, rotation);
    }
    
    public int getRotation() {
        return rotation;
    }
    
    public void setScale(float scale) {
        float oldScale = this.scale;
        this.scale = scale;
        repaint();
        firePropertyChange("scale", oldScale, scale);
    }
    
    public float getScale() {
        return scale;
    }
    
    public void setMouthStyle(int style) {
        int oldStyle = mouthStyle;
        mouthStyle = style;
        firePropertyChange("mouthStyle", oldStyle, style);
        repaint();
    }
    
    public int getMouthStyle() {
        return mouthStyle;
    }
    
    public void setFaceStyle(int style) {
        int oldStyle = faceStyle;
        faceStyle = style;
        firePropertyChange("faceStyle", oldStyle, style);
        repaint();
    }
    
    public int getFaceStyle() {
        return faceStyle;
    }
    
    public void setHairStyle(int style) {
        int oldStyle = hairStyle;
        hairStyle = style;
        firePropertyChange("hairStyle", oldStyle, style);
        repaint();
    }
    
    public int getHairStyle() {
        return hairStyle;
    }
    
    public void setEyeStyle(int style) {
        int oldStyle = eyeStyle;
        eyeStyle = style;
        firePropertyChange("eyeStyle", oldStyle, style);
        repaint();
    }
    
    public int getEyeStyle() {
        return eyeStyle;
    }
    
    public void setNoseStyle(int style) {
        int oldStyle = noseStyle;
        noseStyle = style;
        firePropertyChange("noseStyle", oldStyle, style);
        repaint();
    }
    
    public int getNoseStyle() {
        return noseStyle;
    }
    
    public Dimension getPreferredSize() {
        if (!isPreferredSizeSet()) {
            Image image = getImage("mouth", 0);
            return new Dimension(image.getWidth(null), image.getHeight(null));
        }
        return super.getPreferredSize();
    }
    
    public Dimension getMaximumSize() {
        if (!isMaximumSizeSet()) {
            return getPreferredSize();
        }
        return super.getMaximumSize();
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (empty) {
            return;
        }
        Graphics2D g2 = (Graphics2D)g.create();
        Image image = getImage("face", getFaceStyle());
        int iw = image.getWidth(null);
        int ih = image.getHeight(null);
//        g2.translate(iw / 2, ih / 2);
        g2.translate(getWidth() / 2, getHeight() / 2);
        if (iw != getWidth()) {
            float forcedScale = (float)getWidth() / (float)iw;
            g2.scale(forcedScale, forcedScale);
        }
        float scale = getScale();
        if (scale != 1) {
            g2.scale((double)scale, (double)scale);
        }
        int rotation = getRotation();
        if (rotation != 0) {
            g2.rotate(Math.toRadians(rotation));
        }
        drawImage(g2, "face", getFaceStyle());
        drawImage(g2, "hair", getHairStyle());
        drawImage(g2, "eyes", getEyeStyle());
        drawImage(g2, "nose", getNoseStyle());
        drawImage(g2, "mouth", getMouthStyle());
        g2.dispose();
    }

    private void drawImage(Graphics g, String string, int i) {
        Image image = getImage(string, i);
        g.drawImage(image, -image.getWidth(null) / 2, -image.getHeight(null) / 2, null);
    }

    private Image getImage(String key, int style) {
        String imageName = key + (style + 1) + ".gif";
        Image image = (Image) imageMap.get(imageName);
        if (image == null) {
            System.err.println("name=" + imageName);
            URL imageLoc = getClass().getResource("resources/" + imageName);
            image = new ImageIcon(imageLoc).getImage();
            imageMap.put(imageName, image);
        }
        return image;
    }
}
