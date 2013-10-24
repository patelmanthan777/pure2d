/**
 * 
 */
package com.funzio.pure2D.ui.xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.funzio.pure2D.DisplayObject;
import com.funzio.pure2D.containers.DisplayGroup;
import com.funzio.pure2D.containers.HGroup;
import com.funzio.pure2D.containers.HList;
import com.funzio.pure2D.containers.HWheel;
import com.funzio.pure2D.containers.VGroup;
import com.funzio.pure2D.containers.VList;
import com.funzio.pure2D.containers.VWheel;
import com.funzio.pure2D.shapes.Clip;
import com.funzio.pure2D.shapes.Rectangular;
import com.funzio.pure2D.shapes.Sprite;
import com.funzio.pure2D.shapes.Sprite9;
import com.funzio.pure2D.text.BmfTextObject;
import com.funzio.pure2D.text.TextOptions;
import com.funzio.pure2D.ui.Button;

/**
 * @author long.ngo
 */
public class UIConfig {
    private static final String TAG = UIConfig.class.getSimpleName();

    private static final HashMap<String, Class<? extends DisplayObject>> CLASS_MAP = new HashMap<String, Class<? extends DisplayObject>>();
    static {
        CLASS_MAP.put("Group", DisplayGroup.class);
        CLASS_MAP.put("VGroup", VGroup.class);
        CLASS_MAP.put("HGroup", HGroup.class);
        CLASS_MAP.put("VWheel", VWheel.class);
        CLASS_MAP.put("HWheel", HWheel.class);
        CLASS_MAP.put("VList", VList.class);
        CLASS_MAP.put("HList", HList.class);
        CLASS_MAP.put("Rect", Rectangular.class);
        CLASS_MAP.put("Sprite", Sprite.class);
        CLASS_MAP.put("Sprite9", Sprite9.class);
        CLASS_MAP.put("Clip", Clip.class);
        CLASS_MAP.put("Button", Button.class);
        CLASS_MAP.put("Text", BmfTextObject.class);
    }

    private XmlPullParserFactory mFactory;
    private ArrayList<TextOptions> mFonts = new ArrayList<TextOptions>();

    @SuppressWarnings("unchecked")
    public static Class<? extends DisplayObject> getClassByName(final String name) {
        Log.v(TAG, "getClassByName(): " + name);

        if (CLASS_MAP.containsKey(name)) {
            return CLASS_MAP.get(name);
        } else {

            try {
                Class<?> theClass = Class.forName(name);
                if (theClass.isAssignableFrom(DisplayObject.class)) {
                    return (Class<? extends DisplayObject>) theClass;
                } else {
                    Log.e(TAG, "Class is NOT a DisplayObject: " + name, new Exception());
                }
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Class NOT Found: " + name, e);
            }
        }

        return null;
    }

    public void reset() {
        mFonts.clear();
    }

    public boolean load(final String xmlString) {
        Log.v(TAG, "load()");

        try {
            if (mFactory == null) {
                mFactory = XmlPullParserFactory.newInstance();
            }

            final XmlPullParser xpp = mFactory.newPullParser();
            xpp.setInput(new StringReader(xmlString));

            return load(xpp);
        } catch (Exception e) {
            Log.e(TAG, "XML Parsing Error!", e);
        }

        return false;
    }

    public boolean load(final XmlPullParser parser) {
        Log.v(TAG, "load(): " + parser);

        try {
            int eventType = parser.next();
            String nodeName = "";

            if (eventType == XmlResourceParser.START_DOCUMENT) {
                eventType = parser.next();
            }

            do {
                if (eventType == XmlResourceParser.START_TAG) {
                    nodeName = parser.getName();

                    if (nodeName.equalsIgnoreCase("fonts")) {
                        parseFonts(parser);
                    }

                }

                // next
                eventType = parser.next();
            } while (eventType != XmlResourceParser.END_TAG);
        } catch (Exception e) {
            Log.e(TAG, "XML Parsing Error!", e);
        }

        return false;
    }

    private void parseFonts(final XmlPullParser parser) throws Exception {
        int eventType = -1;
        do {
            eventType = parser.next();
            String nodeName = "";
            if (eventType == XmlResourceParser.START_TAG) {
                nodeName = parser.getName();

                if (nodeName.equalsIgnoreCase("Font")) {
                    final TextOptions options = TextOptions.getDefault();
                    options.id = parser.getAttributeValue(null, "id"); // required

                    if (parser.getAttributeValue(null, "characters") != null) {
                        options.inCharacters = parser.getAttributeValue(null, "characters");
                    }
                    if (parser.getAttributeValue(null, "typeface") != null) {
                        options.inTextPaint.setTypeface(Typeface.create(parser.getAttributeValue(null, "typeface"), TextOptions.getTypefaceStyle(parser.getAttributeValue(null, "style"))));
                    }
                    if (parser.getAttributeValue(null, "size") != null) {
                        options.inTextPaint.setTextSize(Float.valueOf(parser.getAttributeValue(null, "size")));
                    }
                    if (parser.getAttributeValue(null, "color") != null) {
                        options.inTextPaint.setColor(Color.parseColor(parser.getAttributeValue(null, "color")));
                    }
                    if (parser.getAttributeValue(null, "paddingX") != null) {
                        options.inPaddingX = Float.valueOf(parser.getAttributeValue(null, "paddingX"));
                    }
                    if (parser.getAttributeValue(null, "paddingY") != null) {
                        options.inPaddingY = Float.valueOf(parser.getAttributeValue(null, "paddingY"));
                    }

                    // stroke
                    if (parser.getAttributeValue(null, "strokeColor") != null) {
                        options.inStrokePaint = new TextPaint(options.inTextPaint);
                        options.inStrokePaint.setColor(Color.parseColor(parser.getAttributeValue(null, "strokeColor")));
                        if (parser.getAttributeValue(null, "strokeSize") != null) {
                            options.inStrokePaint.setTextSize(Float.valueOf(parser.getAttributeValue(null, "strokeSize")));
                        }
                    }

                    // add to map
                    mFonts.add(options);
                }
            }

            // next
            eventType = parser.next();
        } while (eventType != XmlResourceParser.END_TAG);
    }

    public List<TextOptions> getFonts() {
        return mFonts;
    }
}
