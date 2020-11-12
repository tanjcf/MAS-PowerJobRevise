package com.github.kfcfans.powerjob.server.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL conext 工具类
 *
 * @author jctan
 * @since 2020/9/25

 */
public class SqlConextUtils {
    public  List<String> sqlConextStr(final String query){
        String[] sqlArray =query.split("\n");
        StringBuffer tmp=new StringBuffer();
        List<String> resltSqlList = new ArrayList<>();
        try {
            for (String sql:sqlArray){
                //上一行和下一行拼接需要用空格分开
                tmp.append(sql+" ");
                if (sql.equals("")){
                    resltSqlList.add(tmp.toString().substring(0,tmp.toString().length()-2));
                    tmp=new StringBuffer();
                }

            }
            resltSqlList.add(tmp.toString().substring(0,tmp.toString().length()-1));
            tmp=null;
            return resltSqlList;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
//
//    public static void main(String[] args) {
//        SqlConextUtils SqlConextUtils = new SqlConextUtils();
//        String sql="select * \n" +
//                "form text1;\n" +
//                "\n" +
//                "with a as (select * from text2),\n" +
//                "inset overwrite table select * from a where a.roomid='10010' ;\n";
//       List<String> s= SqlConextUtils.sqlConextStr(sql);
//        System.out.println(s.toString());
//    }
}
