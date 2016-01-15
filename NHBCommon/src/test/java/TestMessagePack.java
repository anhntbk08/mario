import static org.msgpack.template.Templates.tMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.template.StringTemplate;
import org.msgpack.template.Template;

import com.nhb.common.data.msgpkg.GenericTypeTemplate;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class TestMessagePack {

	public static void main(String[] args) throws IOException {
		
		Template<Map<String, Object>> mapTemplate = tMap(StringTemplate.getInstance(),
				GenericTypeTemplate.getInstance());
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("long_value", Long.MAX_VALUE);

		System.out.println("Working with original map: " + map);

		MessagePack mp = new MessagePack();
		ByteOutputStream os = new ByteOutputStream();
		Packer pk = mp.createPacker(os);
		mapTemplate.write(pk, map);
		byte[] bytes = os.getBytes();

		System.out.println(new String(bytes));

		ByteInputStream is = new ByteInputStream(bytes, bytes.length);
		Map<String, Object> map1 = mp.createUnpacker(is).read(mapTemplate);
		System.out.println(map1.get("long_value").getClass());
	}

}
