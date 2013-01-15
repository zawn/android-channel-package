/*
 * Copyright 2013 ZhangZhenli <zhangzhenli@live.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.mimail.ant;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class XmlUtils {

    /**
     * 创建xml文档
     */
    public void createXML(File file) throws Exception {

        // 解析器工厂类
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        // 忽略空格
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        // 解析器
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();

        // xml对象
        Document doc = builder.newDocument();

        // 设置xml版本
        doc.setXmlVersion("1.0");

        // 创建根节点
        Element root = doc.createElement("books");

        // 为根节点添加属性
        root.setAttribute("type", "编程类");
        root.setAttribute("year", "2010");

        // 将根节点添加到document中去,root添加到doc中去后，后面再向root中添加子节点，对与doc同样生效
        doc.appendChild(root);

        /* 设置第一本书 */
        Element bookElement = doc.createElement("book");

        // 设置book节点的id属性
        bookElement.setAttribute("id", "001");

        // 设置name节点
        Element nameElement = doc.createElement("name");

        // 给method设置值
        nameElement.setTextContent("java编程思想");

        // 将method节点添加到page节点
        bookElement.appendChild(nameElement);

        // 创建company节点
        Element companyElement = doc.createElement("company");
        companyElement.setTextContent("清华大学出版社");
        bookElement.appendChild(companyElement);

        // 创建price节点
        Element priceElement = doc.createElement("price");
        priceElement.setTextContent("58.9");
        bookElement.appendChild(priceElement);

        // 将第一本书添加到根节点
        root.appendChild(bookElement);

        /* 设置第二本书 */
        Element bookElement2 = doc.createElement("book");

        // 设置book节点的id属性
        bookElement2.setAttribute("id", "002");

        // name节点
        Element nameElement2 = doc.createElement("name");
        nameElement2.setTextContent("JavaScript高级编程");

        bookElement2.appendChild(nameElement2);

        // company节点
        Element companyElement2 = doc.createElement("company");
        companyElement2.setTextContent("北京理工大学出版社");

        bookElement2.appendChild(companyElement2);

        // price节点
        Element priceElement2 = doc.createElement("price");
        priceElement2.setTextContent("66.5");

        bookElement2.appendChild(priceElement2);

        // 加到root节点
        root.appendChild(bookElement2);

        //保存
        this.saveDocument(doc, file);

    }

    /**
     * 解析整个xml并打印出来
     *
     * @param filePath
     */
    public void transformXML(File file) throws Exception {

        Document doc = this.getDocument(file);

        // 取出根节点
        Element root = doc.getDocumentElement();

        // 解析打印该节点及所有子节点
        this.transformNode(root, "");

    }

    /**
     * 为已有的xml文档增加节点
     *
     * @param filePath
     */
    public void addNode(File file) throws Exception {

        //得到已有的book.xml对象,为其增加几个节点
        Document doc = this.getDocument(file);

        //根节点
        Element root = doc.getDocumentElement();

        //先为已经有的两个book节点添加version节点
        NodeList list = root.getElementsByTagName("name");

        //java编程思想为第一版
        Element version = doc.createElement("version");
        version.setTextContent("第一版");
        //JavaScript高级编程为第四版
        Element version2 = doc.createElement("version");
        version2.setTextContent("第二版");

        //为book节点增加version节点
        int length = list.getLength();
        for (int i = 0; i < length; i++) {
            Node node = list.item(i);
            if (node.getTextContent().trim().equals("java编程思想".trim())) {
                node.getParentNode().appendChild(version);
            } else if (node.getTextContent().trim().equals("JavaScript高级编程".trim())) {
                node.getParentNode().appendChild(version2);
            }
        }

        //新增一个book节点
        Element book = doc.createElement("book");
        book.setAttribute("id", "003");

        Element name = doc.createElement("name");
        name.setTextContent("ExtJS入门教程");
        Element company = doc.createElement("company");
        company.setTextContent("机械工业出版社");
        Element price = doc.createElement("price");
        price.setTextContent("34.5");
        Element version3 = doc.createElement("version");
        version3.setTextContent("第一版");

        book.appendChild(name);
        book.appendChild(company);
        book.appendChild(price);
        book.appendChild(version3);

        root.appendChild(book);

        //保存
        this.saveDocument(doc, file);

    }

    /**
     * 修改节点
     * 我靠,dom方式的修改真的是很麻烦
     *
     * @param file
     * @throws Exception
     */
    public void modifyNode(File file) throws Exception {
        /*
         *将java的售价提高5元，JavaScript减少2元，ExtJS增加11.5元 
         */

        Document doc = this.getDocument(file);

        //先取得所有NodeName为name的节点
        NodeList nameList = doc.getElementsByTagName("name");
        int length = nameList.getLength();
        //为了尽量减少循环设此变量
        int count = 0;
        //循环判断
        for (int i = 0; i < length && count < 3; i++) {
            Node node = nameList.item(i);
            if (node.getTextContent().trim().equals("java编程思想".trim())) {
                node = node.getParentNode();
                node = node.getLastChild();

                //当node节点不为空且node名称不为price时,不停向上面一个节点查找,直到上面在也没有节点
                while (node != null && !node.getNodeName().equals("price")) {
                    node = node.getPreviousSibling();
                }
                if (node != null && node.getNodeName().trim().equals("price".trim())) {
                    String sPrice = this.getString(node.getTextContent());
                    double price = Double.parseDouble(sPrice);
                    price = price + 5;
                    node.setTextContent(new Double(price).toString());
                }
                //修改完一本后记录数加一次
                count++;
            }
            if (node.getTextContent().trim().equals("JavaScript高级编程".trim())) {
                node = node.getParentNode();
                node = node.getLastChild();

                while (node != null && !node.getNodeName().equals("price")) {
                    node = node.getPreviousSibling();
                }
                if (node != null && node.getNodeName().trim().equals("price".trim())) {
                    String sPrice = this.getString(node.getTextContent());
                    double price = Double.parseDouble(sPrice);
                    price = price - 2;
                    node.setTextContent(new Double(price).toString());
                }
                //修改完一本后记录数加一次
                count++;
            }
            if (node.getTextContent().trim().equals("ExtJS入门教程".trim())) {
                node = node.getParentNode();
                node = node.getLastChild();

                //当node节点不为空且node名称不为price时,不停向上面一个节点查找,直到上面在也没有节点
                while (node != null && !node.getNodeName().equals("price")) {
                    node = node.getPreviousSibling();
                }
                if (node != null && node.getNodeName().trim().equals("price".trim())) {
                    String sPrice = this.getString(node.getTextContent());
                    double price = Double.parseDouble(sPrice);
                    price = price + 11.5;
                    node.setTextContent(new Double(price).toString());
                }
                //修改完一本后记录数加一次
                count++;
            }
        }

        this.saveDocument(doc, file);

    }

    /**
     * 删除node节点，当node被删除后，xml文件中会出现空行，我还不知道怎样去掉这些空行
     *
     * @param file
     * @throws Exception
     */
    public void deleteNode(File file) throws Exception {
        /*
         * 删除所有nodeName为verion的节点
         */
        Document doc = this.getDocument(file);

        NodeList list = doc.getElementsByTagName("version");

        /*
         * NodeList特性：当其中包含的node被父节点删除后，NodeList也会随之将之删除，NodeList的length减少1，排序靠后的node会依次
         * 向前移动，占据被移除的node的位置
         * 下面循环中只要NodeList的长度还大于0，便继续循环，始终删除处于0位置的node
         */
        for (int i = 0; list.getLength() > 0;) {
            Node node = list.item(i);
            node.getParentNode().removeChild(node);
        }

        this.saveDocument(doc, file);
    }

    /**
     * 递归方法，解析一个Node及其子Node并打印出来
     *
     * @param Node
     *              要解析的的节点
     * @param space
     *              第一个节点距离页面最左端的空格
     * @throws Exception
     */
    public void transformNode(Node node, String space) throws Exception {

        // 判断该Node对象是否为Element
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            // 格式用空格,子节点头部与父节点相差几个空格
            String addSpace = "    ";

            // 先打印该节点名称及属性
            String head = this.getNodeNameAndAttribute(node, space);
            System.out.print(head);

            // 判断该节点是否有ELEMENT_NODE类型子节点，如果再调用本方法递归
            boolean check = this.checkChildNodes(node);
            if (check) {
                // 如果有子节点，则要换行显示了
                System.out.println();
                // 得到子节点
                NodeList list = node.getChildNodes();
                int length = list.getLength();
                for (int i = 0; i < length; i++) {
                    Node childNode = list.item(i);
                    this.transformNode(childNode, space + addSpace);
                }
            } else {
                String content = node.getTextContent();
                if (content.trim().length() > 0) {
                    System.out.print(content);
                }
            }

            // 打印该节点的尾部信息,为了打印TextContent时没有多余的空格要进行判断
            // 子节点校验为true时这样打印
            if (check) {
                System.out.println(space + "<" + node.getNodeName() + "/>");
            } // 子节点校验为false时这样打印
            else {
                System.out.println("<" + node.getNodeName() + "/>");
            }

        }
    }

    /**
     * 解析每个开始节点的名称及属性
     *
     * @param node
     *              要解析的节点
     * @param space
     *              子节点换行时加几个空格以助显示
     * @return 该节点的xml方式显示
     */
    public String getNodeNameAndAttribute(Node node, String space) {

        StringBuilder buffer = new StringBuilder();
        // 加入打头空格
        buffer.append(space);
        // 节点名
        buffer.append("<").append(node.getNodeName());
        // 解析根节点属性
        if (node.hasAttributes()) {
            NamedNodeMap map = node.getAttributes();
            for (int i = 0; i < map.getLength(); i++) {
                node = map.item(i);
                buffer.append(" ").append(node.getNodeName()).append("=\"").append(node.getNodeValue()).append("\"");
            }
            buffer.append(">");
        } else {
            buffer.append(">");
        }
        return buffer.toString();
    }

    /**
     * 判断一个节点是否含有最少一个ELEMENT_NODE类型的子节点
     *
     * @param node
     * @return true表示该节点含有子节点且至少有一个子节点是ELEMENT_NODE类型的
     *         false表示该节点没有子节点或子节点全不是ELEMENT_NODE类型的
     */
    public boolean checkChildNodes(Node node) {
        boolean check = false;
        if (node.hasChildNodes()) {
            NodeList list = node.getChildNodes();
            int length = list.getLength();
            for (int i = 0; i < length && check == false; i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    check = true;
                }
            }
        }
        return check;
    }

    /**
     * 过滤null、"null"
     *
     * @param s
     * @return
     * @throws Exception
     */
    public String getString(String s) throws Exception {
        if (s == null || s.trim().equals("null") || s.trim().length() < 1) {
            return "";
        } else {
            return s.trim().toString();
        }
    }

    /**
     * 通过dom提供的document解析工厂得到document
     *
     * @param file
     * @return
     * @throws Exception
     */
    public Document getDocument(File file) throws Exception {

        // 解析器工厂类
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        // 忽略空格
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        // 解析器
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();

        // xml对象
        Document doc = builder.parse(file);

        return doc;
    }

    /**
     * 保存生成或修改完的xml文件
     *
     * @param doc
     * @param file
     * @throws Exception
     */
    public void saveDocument(Document doc, File file) throws Exception {

        // 开始把Document映射到文件
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // 设置输出结果
        DOMSource source = new DOMSource(doc);

        // 判断文件是否存在，如不存在则创建
        if (!file.exists()) {
            file.createNewFile();
        }

        // 设置输入源
        Result xmlResult = new StreamResult(file);

        // 输出xml文件
        transformer.transform(source, xmlResult);
    }

    /*
     * 在main方法中找到你自己存放xml文件的地址，把filepath修改下就可以了
     */
    public static void main(String[] args) throws Exception {

        // 获取工程的绝对路径
        String projectRealPath = System.getProperties().getProperty("user.dir");
        String xmlFoderPath = projectRealPath + "\\WebRoot\\xml";
        String filePath = xmlFoderPath + "\\" + "book.xml";
        File file = new File(filePath);


        //调用下面几个方法可以创建xml，添加节点，删除节点，修改内容，解析xml

        //new XMLDomUtil().createXML(file);
        //new XMLDomUtil().addNode(file);
//		new XMLDomUtil().deleteNode(file);
        //new XMLDomUtil().modifyNode(file);
//		new XMLDomUtil().transformXML(file);

    }
}
