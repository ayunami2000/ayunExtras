package me.ayunami2000.ayunextras;

import org.bukkit.entity.Player;

import java.util.*;

public class Captcha {
    private static final Random random = new Random();
    private static final String base = "Sorry for bad england ,\nplease complete captcha: type";
    private static final String base2 = "and then";

    public Player player;
    public String key1;
    public String key2;
    public boolean passed1 = false;

    public Captcha(Player pl) {
        player = pl;
        key1 = randCmd() + getSaltString(5 + random.nextInt(3));
        key2 = randCmd() + getSaltString(5 + random.nextInt(3));
        pl.sendMessage(typoGenFull(base) + " " + key1 + " " + typoGenFull(base2) + " " + key2);
    }

    public boolean check(String in) {
        return in.equals(passed1 ? key2 : key1);
    }

    public static Captcha get(Set<Captcha> cs, Player p) {
        return cs.stream().filter(c -> c.player.getName().equals(p.getName())).findFirst().orElse(null);
    }

    private static final String saltChars = "abcdefghijklmnopqrstuvwxyz1234567890";

    private static String getSaltString(int len) {
        StringBuilder salt = new StringBuilder();
        while (salt.length() < len) {
            int index = (int) (random.nextFloat() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();

    }

    private static String randCmd() {
        return random.nextBoolean() ? "/" : "";
    }

    private static String typoGenFull(String in) {
        String[] pieces = in.split(" ");
        for (int i = 0; i < pieces.length; i++) {
            pieces[i] = typoGen(pieces[i]);
        }
        return String.join(" ", pieces);
    }

    //https://gist.github.com/iwek/4121650
    private static String typoGen(String str) {
        //define proximity arrays
        Map<Character, char[]> array_prox = new HashMap<>();
        array_prox.put('a', new char[] {'q', 'w', 'z', 'x'});
        array_prox.put('b', new char[] {'v', 'f', 'g', 'h', 'n'});
        array_prox.put('c', new char[] {'x', 's', 'd', 'f', 'v'});
        array_prox.put('d', new char[] {'x', 's', 'w', 'e', 'r', 'f', 'v', 'c'});
        array_prox.put('e', new char[] {'w', 's', 'd', 'f', 'r'});
        array_prox.put('f', new char[] {'c', 'd', 'e', 'r', 't', 'g', 'b', 'v'});
        array_prox.put('g', new char[] {'r', 'f', 'v', 't', 'b', 'y', 'h', 'n'});
        array_prox.put('h', new char[] {'b', 'g', 't', 'y', 'u', 'j', 'm', 'n'});
        array_prox.put('i', new char[] {'u', 'j', 'k', 'l', 'o'});
        array_prox.put('j', new char[] {'n', 'h', 'y', 'u', 'i', 'k', 'm'});
        array_prox.put('k', new char[] {'u', 'j', 'm', 'l', 'o'});
        array_prox.put('l', new char[] {'p', 'o', 'i', 'k', 'm'});
        array_prox.put('m', new char[] {'n', 'h', 'j', 'k', 'l'});
        array_prox.put('n', new char[] {'b', 'g', 'h', 'j', 'm'});
        array_prox.put('o', new char[] {'i', 'k', 'l', 'p'});
        array_prox.put('p', new char[] {'o', 'l'});
        array_prox.put('r', new char[] {'e', 'd', 'f', 'g', 't'});
        array_prox.put('s', new char[] {'q', 'w', 'e', 'z', 'x', 'c'});
        array_prox.put('t', new char[] {'r', 'f', 'g', 'h', 'y'});
        array_prox.put('u', new char[] {'y', 'h', 'j', 'k', 'i'});
        array_prox.put('v', new char[] {'c', 'd', 'f', 'g', 'b'});
        array_prox.put('w', new char[] {'q', 'a', 's', 'd', 'e'});
        array_prox.put('x', new char[] {'z', 'a', 's', 'd', 'c'});
        array_prox.put('y', new char[] {'t', 'g', 'h', 'j', 'u'});
        array_prox.put('z', new char[] {'x', 's', 'a'});
        array_prox.put('1', new char[] {'q', 'w'});
        array_prox.put('2', new char[] {'q', 'w', 'e'});
        array_prox.put('3', new char[] {'w', 'e', 'r'});
        array_prox.put('4', new char[] {'e', 'r', 't'});
        array_prox.put('5', new char[] {'r', 't', 'y'});
        array_prox.put('6', new char[] {'t', 'y', 'u'});
        array_prox.put('7', new char[] {'y', 'u', 'i'});
        array_prox.put('8', new char[] {'u', 'i', 'o'});
        array_prox.put('9', new char[] {'i', 'o', 'p'});
        array_prox.put('0', new char[] {'o', 'p'});

        Set<String> arr = new HashSet<>();

        for(int a=0; a<str.length(); a++) {
            char fard = str.charAt(a);
            char[] temp = array_prox.getOrDefault(fard, new char[] { fard });
            for(int b=0; b<temp.length; b++) {
                String typo = str.substring(0, a) + temp[b] + str.substring(a + 1);
                arr.add(typo);
            }
        }

        return arr.stream().skip(random.nextInt(arr.size())).findFirst().orElse(null);
    }
}
