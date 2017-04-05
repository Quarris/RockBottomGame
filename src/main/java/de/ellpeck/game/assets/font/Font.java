package de.ellpeck.game.assets.font;

import de.ellpeck.game.util.Vec2;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

public class Font{

    private static final Pattern FORMATTING_PATTERN = Pattern.compile("&[a-z0-9]");
    private static final Color[] COLORS_BY_FORMATTING_CODE = new Color[]{Color.black, Color.darkGray, Color.gray, Color.lightGray, Color.white, Color.yellow, Color.orange, Color.red, Color.pink, Color.magenta, Color.black, Color.green, Color.transparent};
    private static final String FORMATTING_CODES = "0123456789abcde";

    private final String name;
    private final Image image;

    private final Map<Character, Vec2> characters;

    private final int charWidth;
    private final int charHeight;

    public Font(String name, Image image, int widthInChars, int heightInChars, Map<Character, Vec2> characters){
        this.name = name;
        this.image = image;
        this.characters = characters;

        this.charWidth = image.getWidth()/widthInChars;
        this.charHeight = image.getHeight()/heightInChars;
    }

    public static Font fromStream(InputStream imageStream, InputStream infoStream, String name) throws IOException, SlickException{
        Image image = new Image(imageStream, name, false);
        image.setFilter(Image.FILTER_NEAREST);

        BufferedReader reader = new BufferedReader(new InputStreamReader(infoStream));

        int width = 0;
        int heightIndex = 0;
        Map<Character, Vec2> characters = new HashMap<>();

        String line = reader.readLine();
        while(line != null){
            if(!line.isEmpty()){
                char[] chars = line.toCharArray();
                if(chars.length > width){
                    width = chars.length;
                }

                for(int i = 0; i < chars.length; i++){
                    characters.put(chars[i], new Vec2(i, heightIndex));
                }
            }
            heightIndex++;

            line = reader.readLine();
        }

        return new Font(name, image, width, heightIndex, characters);
    }

    public void drawStringFromRight(float x, float y, String s, float scale){
        this.drawString(x-this.getWidth(s, scale), y, s, scale);
    }

    public void drawCenteredString(float x, float y, String s, float scale, boolean centeredOnY){
        this.drawString(x-this.getWidth(s, scale)/2F, centeredOnY ? (y-this.getHeight(scale)/2F) : y, s, scale);
    }

    public void drawString(float x, float y, String s, float scale){
        Color color = Color.white;
        float xOffset = 0F;

        char[] characters = s.toCharArray();
        for(int i = 0; i < characters.length; i++){
            if(characters[i] == '&' && i < characters.length-1){
                int formatIndex = FORMATTING_CODES.indexOf(characters[i+1]);
                if(formatIndex >= 0){
                    color = COLORS_BY_FORMATTING_CODE[formatIndex];
                    i++;
                    continue;
                }
            }

            this.drawCharacter(x+xOffset, y, characters[i], scale, color);
            xOffset += (float)this.charWidth*scale;
        }
    }

    public void drawCharacter(float x, float y, char character, float scale, Color color){
        if(character != ' '){
            Vec2 pos = this.characters.get(character);

            if(pos == null){
                pos = this.characters.get('?');
                this.characters.put(character, pos);

                Log.warn("Character "+character+" is missing from font with name "+this.name+"!");
            }

            int srcX = pos.getX()*this.charWidth;
            int srcY = pos.getY()*this.charHeight;

            this.image.draw(x, y, x+(float)this.charWidth*scale, y+(float)this.charHeight*scale, srcX, srcY, srcX+this.charWidth, srcY+this.charHeight, color);
        }
    }

    public String removeFormatting(String s){
        return FORMATTING_PATTERN.matcher(s).replaceAll("");
    }

    public float getWidth(String s, float scale){
        return (float)this.charWidth*(float)this.removeFormatting(s).length()*scale;
    }

    public float getHeight(float scale){
        return (float)this.charHeight*scale;
    }

    public List<String> splitTextToLength(int length, float scale, String... lines){
        return this.splitTextToLength(length, scale, Arrays.asList(lines));
    }

    public List<String> splitTextToLength(int length, float scale, List<String> lines){
        List<String> result = new ArrayList<>();
        String accumulated = "";

        for(String line : lines){
            String[] words = line.split(" ");

            for(String word : words){
                if(this.getWidth(accumulated+word, scale) >= length){
                    result.add(accumulated.trim());
                    accumulated = word+" ";
                }
                else{
                    accumulated += word+" ";
                }
            }

            result.add(accumulated.trim());
            accumulated = "";
        }

        return result;
    }
}