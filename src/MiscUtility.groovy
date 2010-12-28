import net.sf.json.*
import java.util.zip.*

class MiscUtility {

   // utility method to convert json object to map and array
   static convertToMapAndArray(jsonObj) {
      switch(jsonObj) {
         case List: 
            return jsonObj.inject([]) { l, elem -> l << convertToMapAndArray(elem); l }
         case Map:  
            return jsonObj.inject([:]) { m, entry -> m[entry.key] = convertToMapAndArray(entry.value); m }
         case JSONNull:
            return null
         default:   
            return jsonObj
      }
   }

   static deflateObjectToByteArray(obj) {
      def bytes = new ByteArrayOutputStream()
      def outstream = new ObjectOutputStream(new GZIPOutputStream(bytes))
      outstream.writeObject(obj)
      outstream.close()
      bytes.toByteArray()
   }

   static inflateByteArrayToObj(bytearray) {
      return new ObjectInputStream(
         new GZIPInputStream(new ByteArrayInputStream(bytearray))).readObject()

   }
}


