/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.mimail.ant;

/**
 *
 * @author Yutian
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String s = "安智,机锋,豌豆荚,腾讯应用,安卓,360应用,91应用,百度应用,应用汇,木蚂蚁,N多,智汇云,泡椒,优亿";
        String toPinYin = Utils.toPinYin(s);
        String[] split = toPinYin.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String string = split[i];
            sb.append(string).append(";");
        }
        String toString = sb.toString();
        System.out.println(toString);
        String[] split1 = toString.split(";");
        for (int i = 0; i < split1.length; i++) {
            String string2 = split1[i];
            System.out.println(string2);
            System.out.println("--------------------------");
            
        }
                
    }

}
