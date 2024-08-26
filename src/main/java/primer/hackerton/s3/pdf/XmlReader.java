package primer.hackerton.s3.pdf;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class XmlReader {
    private String path = "C:\\Users\\정연호\\Desktop\\primer\\CORPCODE.xml";
    private String outputPath = "output.csv";


    public Map<String, Company> parse(){
        File file = new File(path);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        Map<String, Company> map = new HashMap<>();

        try {
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            document.getDocumentElement().normalize();
            NodeList list = document.getElementsByTagName("list");

            for(int i =0; i< list.getLength(); i++){
                Node item = list.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE){
                    Element eElement = (Element) item;
                    String code = eElement.getElementsByTagName("corp_code").item(0).getTextContent();
                    String name = eElement.getElementsByTagName("corp_name").item(0).getTextContent();
                    String modifyDate = eElement.getElementsByTagName("modify_date").item(0).getTextContent();

                    if(map.containsKey(name)){
                        Company com = map.get(name);

                        LocalDate date1 = LocalDate.parse(com.getModifyDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));
                        LocalDate date2 = LocalDate.parse(modifyDate, DateTimeFormatter.ofPattern("yyyyMMdd"));

                        if(date2.isAfter(date1)){
                            map.put(name, new Company(code, name, modifyDate));

                        }

                    } else {
                        map.put(name, new Company(code, name, modifyDate));
                    }
                }
            }


        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }

        return map;
    }

    public void write(Map<String, Company> map) {
        try {
            File file = new File(outputPath);

            // 부모 디렉토리가 있을 경우에만 디렉토리 생성
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("corp_code,corp_name,modify_date");
                writer.newLine();
                for (Map.Entry<String, Company> entry : map.entrySet()) {
                    Company company = entry.getValue();
                    writer.write(company.getCode() + "," + company.getName() + "," + company.getModifyDate());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing CSV file: " + e.getMessage(), e);
        }
    }


}
