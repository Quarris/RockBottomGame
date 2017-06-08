package de.ellpeck.rockbottom.api.assets.font;

import de.ellpeck.rockbottom.api.util.Pos2;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Font{

    private final String name;
    private final Image image;

    private final Map<Character, Pos2> characters;

    private final int charWidth;
    private final int charHeight;

    public Font(String name, Image image, int widthInChars, int heightInChars, Map<Character, Pos2> characters){
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
        Map<Character, Pos2> characters = new HashMap<>();

        String line = reader.readLine();
        while(line != null){
            if(!line.isEmpty()){
                char[] chars = line.toCharArray();
                if(chars.length > width){
                    width = chars.length;
                }

                for(int i = 0; i < chars.length; i++){
                    characters.put(chars[i], new Pos2(i, heightIndex));
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

    public void drawFadingString(float x, float y, String s, float scale, float fadeTotal, float fadeInEnd, float fadeOutStart){
        Color color = new Color(Color.white);

        if(fadeTotal <= fadeInEnd){
            color.a *= fadeTotal/fadeInEnd;
        }
        else if(fadeTotal >= fadeOutStart){
            color.a *= 1F-(fadeTotal-fadeOutStart)/(1F-fadeOutStart);
        }

        this.drawString(x, y, s, scale, color);
    }

    public void drawString(float x, float y, String s, float scale){
        this.drawString(x, y, s, scale, Color.white);
    }

    public void drawString(float x, float y, String s, float scale, Color color){
        this.drawString(x, y, s, 0, s.length(), scale, color);
    }

    public void drawCutOffString(float x, float y, String s, float scale, int length, boolean fromRight, boolean basedOnCharAmount){
        int strgLength = s.length();

        int amount = 0;
        String accumulated = "";

        for(int i = 0; i < strgLength; i++){
            if(fromRight){
                accumulated = s.charAt(strgLength-1-i)+accumulated;
            }
            else{
                accumulated += s.charAt(i);
            }

            amount++;

            if((basedOnCharAmount ? this.removeFormatting(accumulated).length() : this.getWidth(accumulated, scale)) >= length){
                break;
            }
        }

        if(fromRight){
            this.drawString(x, y, s, strgLength-amount, strgLength, scale, Color.white);
        }
        else{
            this.drawString(x, y, s, 0, amount, scale, Color.white);
        }
    }

    public void drawSplitString(float x, float y, String s, float scale, int length){
        List<String> split = this.splitTextToLength(length, scale, true, s);

        for(String string : split){
            this.drawString(x, y, string, scale);
            y += this.getHeight(scale);
        }
    }

    private void drawString(float x, float y, String s, int drawStart, int drawEnd, float scale, Color color){
        float xOffset = 0F;

        char[] characters = s.toCharArray();
        for(int i = 0; i < Math.min(drawEnd, characters.length); i++){
            FormattingCode code = FormattingCode.getFormat(s, i);
            if(code != FormattingCode.NONE){
                color = code.getColor();

                i += code.getLength()-1;
                continue;
            }

            if(i >= drawStart){
                this.drawCharacter(x+xOffset, y, characters[i], scale, color);
                xOffset += (float)this.charWidth*scale;
            }
        }
    }

    public void drawCharacter(float x, float y, char character, float scale, Color color){
        if(character != ' '){
            Pos2 pos = this.characters.get(character);

            if(pos == null){
                pos = this.characters.get('?');
                this.characters.put(character, pos);

                Log.warn("Character "+character+" is missing from font with name "+this.name+"!");
            }

            if(pos != null){
                int srcX = pos.getX()*this.charWidth;
                int srcY = pos.getY()*this.charHeight;

                this.image.draw(x, y, x+(float)this.charWidth*scale, y+(float)this.charHeight*scale, srcX, srcY, srcX+this.charWidth, srcY+this.charHeight, color);
            }
        }
    }

    public String removeFormatting(String s){
        String newString = "";
        for(int i = 0; i < s.length(); i++){
            FormattingCode code = FormattingCode.getFormat(s, i);
            if(code != FormattingCode.NONE){
                i += code.getLength()-1;
            }
            else{
                newString += s.charAt(i);
            }
        }
        return newString;
    }

    public float getWidth(String s, float scale){
        return (float)this.charWidth*(float)this.removeFormatting(s).length()*scale;
    }

    public float getHeight(float scale){
        return (float)this.charHeight*scale;
    }

    public List<String> splitTextToLength(int length, float scale, boolean wrapFormatting, String... lines){
        return this.splitTextToLength(length, scale, wrapFormatting, Arrays.asList(lines));
    }

    public List<String> splitTextToLength(int length, float scale, boolean wrapFormatting, List<String> lines){
        List<String> result = new ArrayList<>();
        String accumulated = "";

        for(String line : lines){
            FormattingCode trailingCode = FormattingCode.NONE;

            for(String subLine : line.split("\n")){
                String[] words = subLine.split(" ");

                for(String word : words){
                    if(wrapFormatting){
                        for(int i = 0; i < word.length()-1; i++){
                            FormattingCode format = FormattingCode.getFormat(word, i);
                            if(format != FormattingCode.NONE){
                                trailingCode = format;
                            }
                        }
                    }

                    if(this.getWidth(accumulated+word, scale) >= length){
                        result.add(accumulated.trim());
                        accumulated = trailingCode+word+" ";
                    }
                    else{
                        accumulated += word+" ";
                    }
                }

                result.add(accumulated.trim());
                accumulated = trailingCode.toString();
            }

            accumulated = "";
        }

        return result;
    }
}
