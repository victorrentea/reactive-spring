package victor.training.reactive.intro.whyreactive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class Data {

   public static final List<String> IDS = asList("1", "2", "3", "4", "5");

   public static final  Map<String, Integer> STATS = new HashMap<>();
   static {
      STATS.put("1",103);
      STATS.put("2",104);
      STATS.put("3",105);
      STATS.put("4",106);
      STATS.put("5",121);
   }

   public static final  Map<String, String> NAMES = new HashMap<>();
   static {
      NAMES.put("1","NameJoe");
      NAMES.put("2","NameBart");
      NAMES.put("3","NameHenry");
      NAMES.put("4","NameNicole");
      NAMES.put("5","NameABSLAJNFOAJNFOANFANSF");
   }
}
