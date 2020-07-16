package me.nao;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.nao.commands.PrivCommand;
import me.nao.commands.GuildCommand;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.io.File;


public class Bot {
    private Bot() throws LoginException {
        @SuppressWarnings("deprecation")
		final JDA jda = new JDABuilder( AccountType.BOT )
                .setToken( "your token here" ).build();

        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setPrefix( "your prefix here" );
        builder.setOwnerId( "your id here" );
        builder.setActivity( Activity.watching( "activity here" ) );

        CommandClient client = builder.build();
        jda.addEventListener( client );
        jda.addEventListener( new GuildCommand() );
        jda.addEventListener( new PrivCommand() );
    }

    public static void main(String[] args) throws LoginException {
        long enable = System.currentTimeMillis();

        readFromFiles();
        new Bot();
        System.out.println( "Bot enabled in " + ( System.currentTimeMillis() - enable ) + "ms :D" );
    }

    private static void readFromFiles() {
        try {
            File file = new File("contents.txt");
            Scanner sc = new Scanner(file);

            System.out.println( "start to append contents." );
            // Artwork name | Character1 Character2... | Author | Source/Webpage | picture url | Tag1 Tag2 ...
            while ( sc.hasNextLine() ) {
                G.Content newCont = Bot.G.ContentParser( sc.nextLine() );
                String imageUrl = sc.nextLine();
                newCont.url = imageUrl;
                if ( G.contents.containsKey( newCont.serie ) )
                    System.out.println( "================ Repeated data string detected ================" );
                Bot.G.contents.put( newCont.serie, newCont );

                for ( int i = 0 ; i < newCont.tags.size() ; i++ ) {
                    Bot.G.contentTags.add( newCont.serie + newCont.tags.get( i ) );
                }
            }

            Bot.G.newContent = false;
            Bot.G.tempKey = "";
        }
        catch ( Exception e ) {
            try {
                File myFile = new File("contents.txt");
                if (myFile.createNewFile()) {
                    System.out.println("File created: " + myFile.getName());
                } else {
                    System.out.println("File already exists.");
                }
            } catch (IOException exception) {
                System.out.println("An error occurred.");
                exception.printStackTrace();
            }
        }
    }

    public static class G {
    	
        public static Bot.G.Content ContentParser(String arg) {
            // Artwork name | Character1 Character2... | Author | Source/Webpage | Tag1 Tag2 ...
            // Picture file or Picture URL

            // E.g.
            // Naruto Fight | Naruto Sasuke | Nao | https://www.myartworkblabla.com | tag1 tag2 ...
            // https://...

            // serie
            String serie = "";
            int j = 0;
            while ( arg.charAt( j ) != '|' ) {
                serie += arg.charAt( j );
                j++;
            }
            j++;
            serie = serie.trim();

            // names
            int tempInt = j;
            j = moveToNext( arg, j );

            List<String> names = Arrays.asList( arg.substring( tempInt, j ).trim().split( "\\s+" ) );
            List<String> nameList = new ArrayList<>();
            for ( String name : names ) {
            	List<String> temp = Arrays.asList(name.replace("(", " ").replace(")", " ").replace("（", " ").replace("）", " ").trim().split("\\s+") );
    			nameList.addAll( temp );
            }
            
            for ( String name : nameList )
            	System.out.println( name );

            j++;

            // source
            tempInt = j;
            j = moveToNext( arg, j );

            String artist = "";
            artist = arg.substring( tempInt, j ).trim();
            j++;

            // sourceUrl
            tempInt = j;
            j = moveToNext( arg, j );
            String url = arg.substring( tempInt, j );
            String sourceUrl = url.trim();
            j++;

            // tags
            String tagString = "";
            for ( ; j < arg.length() ; j++ ) {
                tagString += arg.charAt( j );
            }

            List<String> tags = Arrays.asList( tagString.trim().split( "\\s+" ) );
            tags = new ArrayList<>( tags );

            for ( String name : nameList ) {
                tags.add( name );
            }
            tags.add( artist );
            Bot.G.newContent = true;
            Bot.G.tempKey = serie;
            Bot.G.tempArg = arg;

            return new Bot.G.Content( serie, nameList, artist, sourceUrl, "", tags );
        }

        public static int moveToNext( String arg, int cur ) {
            String temp = "";
            
            // arg.getChar( cur ) != '|'
            while ( arg.charAt( cur ) != '|' ) {
                cur++;
                if ( arg.charAt( cur ) == '|' && arg.charAt(cur + 1) == '|' ) {
                	cur += 2;
                }
            }

            return cur;
        }

        public static class Content {
            public String serie;
            public List<String> names;
            public String source;
            public String sourceLink;
            public String url;
            public List<String> tags;

            public Content(String serie, List<String> names, String source, String sourceLink, String url, List<String> tags) {
                this.serie = serie;
                this.names = names;
                this.source = source;
                this.sourceLink = sourceLink;
                this.url = url;
                this.tags = tags;
            }
        }

        public static HashMap<String, Content> contents = new HashMap<String, Content>();
        public static HashSet<String> contentTags = new HashSet<String>();

        public static boolean newContent = false;
        public static String tempKey = "";
        public static String tempArg = "";
        public static User tempUser = null;
    }
}
