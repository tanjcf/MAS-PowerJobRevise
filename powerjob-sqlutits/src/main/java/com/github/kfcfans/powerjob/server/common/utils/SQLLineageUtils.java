package com.github.kfcfans.powerjob.server.common.utils;

import com.github.kfcfans.powerjob.server.common.utils.user.TowTuple;
import org.apache.hadoop.hive.ql.lib.*;
import org.apache.hadoop.hive.ql.parse.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class SQLLineageUtils implements NodeProcessor {

    // 存放输入表
    TreeSet<String> inputTableList = new TreeSet<String>();

    // 存放目标表
    TreeSet<String> outputTableList = new TreeSet<String>();

    //存放with子句中的别名, 最终的输入表是 inputTableList减去withTableList
    TreeSet<String> withTableList = new TreeSet<String>();
    //映射表
    TreeSet<String> linkTableList = new TreeSet<String>();
    TreeSet<String> lineTableList = new TreeSet<String>();
    TreeSet<String> lineWhereTableList = new TreeSet<String>();
    TreeSet<String> taleWhereList = new TreeSet<String>();
    TreeSet<String> tableLineList = new TreeSet<String>();
    TreeSet<String> tableFieldList= new TreeSet<String>();
    //关联关系
    ASTNode tableFrom = new ASTNode();

    public ASTNode getTableFrom() { return tableFrom; }
    //映射表名 表 字段
    public Map<String, TowTuple> asMap =new HashMap<String, TowTuple>();

    public TreeSet<String> getTableLineList() {
        return tableLineList; }
    public TreeSet<String> getTableFieldList() { return tableFieldList; }
    public TreeSet<String> getLineTableList() { return lineTableList; }
    public TreeSet<String> getLineWhereTableList() { return lineWhereTableList; }
    public TreeSet getInputTableList() {
        return inputTableList;
    }
    public TreeSet getOutputTableList() {
        return outputTableList;
    }
    public TreeSet getWithTableList() { return withTableList; }
    public TreeSet getLinkTableList(){ return linkTableList; }
    public TreeSet<String> getTaleWhereList() {return taleWhereList;}
    public void clearTreeSetList(){linkTableList.clear();lineWhereTableList.clear();lineTableList.clear();taleWhereList.clear();}
    public Object myProcess(final ASTNode ASTnd) throws  SemanticException{
        switch (ASTnd.getToken().getType()){
            //with......
            case HiveParser.TOK_CTE:{
                for (int i=0; i < ASTnd.getChildCount(); i++){

                    /**获取到对应映射，名称(映射语句,映射名称)*/
                    String intoTableName =ASTnd.getChild(i).getChild(1).toStringTree().toString();
                    /**获取映射表名和表以及条件(from insert)*/
                    clearTreeSetList();
                    //提取映射表
                    getTable((ASTNode) ASTnd.getChild(i).getChild(0));
                    asMap.put(intoTableName,new TowTuple((TreeSet<String>) getLinkTableList().clone(),(TreeSet<String>) getLineTableList().clone(),(TreeSet<String>) getLineWhereTableList().clone(),(TreeSet<String>) getTaleWhereList().clone()));

                    clearTreeSetList();




                }

                break;
            }
            //from 条件
            case HiveParser.TOK_FROM:{
                this.tableFrom=ASTnd;
                break;
            }
            //
            case HiveParser.TOK_INSERT:{
                for (int i=0;i<ASTnd.getChildCount();i++){
                    if (ASTnd.getChild(i).toString().equals("TOK_SELECT")){
                        getTable((ASTNode) ASTnd.getChild(i));
                        this.tableLineList= (TreeSet<String>) getLineTableList().clone();

                        clearTreeSetList();

                    }
                }
                break;
            }

            default:{
                System.out.println("comnot myProcess this sql,you call myProcess switch case HiveParser.TOK_"+ASTnd.getName());
                break;
            }

        }
        return null;
    }
    private Serializable getTableLine(final  ASTNode A){
        if (A.getToken().getType()==HiveParser.TOK_SELEXPR){
            String lineName;
            ASTNode lineTree = (ASTNode) A.getChild(0);
            if (lineTree.toString().equals("TOK_FUNCTION")||lineTree.getChildCount()==0||lineTree.toString().equals("TOK_FUNCTIONDI")) {

                lineName=lineTree.getChild(0)+"||"+lineTree.toStringTree();
                lineWhereTableList.add(lineName);

            }
            lineName =(lineTree.getChildCount() == 1) ? BaseSemanticAnalyzer.getUnescapedName((ASTNode) lineTree.getChild(0)) : BaseSemanticAnalyzer.getUnescapedName((ASTNode) lineTree.getChild(0).getChild(0)) + "." + lineTree.getChild(1);
            lineTableList.add(lineName);

        }
        for (int i=0;i<A.getChildCount();i++){

            getTableLine((ASTNode)A.getChild(i));
        }

        return null;
    }
    //递归找表出所有的表和字段
    private Serializable getTable(final ASTNode A){

        switch (A.getToken().getType()){
            //获取表和映射名称
            case HiveParser.TOK_TABREF:{
                ASTNode tabTree = (ASTNode) A.getChild(0);
                String fromName = (tabTree.getChildCount() == 1) ? BaseSemanticAnalyzer.getUnescapedName((ASTNode) tabTree.getChild(0)) : BaseSemanticAnalyzer.getUnescapedName((ASTNode) tabTree.getChild(0)) + "." + tabTree.getChild(1);

                if (A.getChild(1)!=null){
                    fromName=A.getChild(1)+"@"+fromName;
                }
                linkTableList.add(fromName);
                break;
            }
            //获取列名和表关系
            case HiveParser.TOK_SELEXPR:{
                String lineName;
                ASTNode lineTree = (ASTNode) A.getChild(0);
                if (lineTree.toString().equals("TOK_FUNCTION")||lineTree.getChildCount()==0||lineTree.toString().equals("TOK_FUNCTIONDI")) {

                    lineName=lineTree.getChild(0)+"||"+lineTree.toStringTree();
                    lineWhereTableList.add(lineName);
                    break;
                }
                lineName =(lineTree.getChildCount() == 1) ? BaseSemanticAnalyzer.getUnescapedName((ASTNode) lineTree.getChild(0)) : BaseSemanticAnalyzer.getUnescapedName((ASTNode) lineTree.getChild(0).getChild(0)) + "." + lineTree.getChild(1);
                lineTableList.add(lineName);
                break;
            }
            //获悉条件
            case HiveParser.TOK_WHERE: case HiveParser.TOK_JOIN:{
                ASTNode whereTree = (ASTNode) A;
                taleWhereList.add(A.toStringTree());
                break;
            }

        }

        for (int i=0;i<A.getChildCount();i++){

            getTable((ASTNode)A.getChild(i));
        }

        return null;
    }
    private Serializable getCreateTable(final ASTNode A){
        if (A==null) {
            return null;

        }
        if (A.toString().equals("TOK_TABCOL")) {
            String temp = A.getChild(0)+":"+A.getChild(1);
            tableLineList.add(temp);
        }
        for (int i=0;i<A.getChildCount();i++){

            getCreateTable((ASTNode)A.getChild(i));
        }
        return null;
    }
    public Object process(Node nd, Stack stack, NodeProcessorCtx procCtx, Object... nodeOutputs) throws SemanticException {
        ASTNode pt = (ASTNode) nd;
        switch (pt.getToken().getType()) {
            //create语句
            case HiveParser.TOK_CREATETABLE: {
                String createName = BaseSemanticAnalyzer.getUnescapedName((ASTNode) pt.getChild(0));

                outputTableList.add(createName);
                break;
            }

            //insert语句
            case HiveParser.TOK_TAB: {
                String insertName = BaseSemanticAnalyzer.getUnescapedName((ASTNode) pt.getChild(0));
                outputTableList.add(insertName);
                break;
            }

            //from语句
            case HiveParser.TOK_TABREF: {
                ASTNode tabTree = (ASTNode) pt.getChild(0);
                String fromName = (tabTree.getChildCount() == 1) ? BaseSemanticAnalyzer.getUnescapedName((ASTNode) tabTree.getChild(0)) : BaseSemanticAnalyzer.getUnescapedName((ASTNode) tabTree.getChild(0)) + "." + tabTree.getChild(1);
                inputTableList.add(fromName);
                break;
            }

            // with.....语句
            case HiveParser.TOK_CTE: {

                for (int i = 0; i < pt.getChildCount(); i++) {
                    ASTNode temp = (ASTNode) pt.getChild(i);
                    String cteName = BaseSemanticAnalyzer.getUnescapedName((ASTNode) temp.getChild(1));
                    withTableList.add(cteName);
                }
                break;
            }
            case HiveParser.TOK_TABCOL:{
                String temp = pt.getChild(0)+":"+pt.getChild(1);
                tableFieldList.add(temp);
                break;
            }
        }
        return null;
    }

    /**
     *
     * @param query
     * @throws ParseException
     * @throws SemanticException
     */
    public void getLineageInfo(String query) throws ParseException, SemanticException {

        ParseDriver pd = new ParseDriver();
        ASTNode tree = pd.parse(query);
        if (tree.getChild(0).toString().equals("TOK_CREATETABLE")){
            getCreateTable(tree);
        }
        while ((tree.getToken() == null) && (tree.getChildCount() >= 0)) {
            tree = (ASTNode) tree.getChild(0);
        }

        for (int i=0;i<tree.getChildCount();i++){
            ASTNode temp = (ASTNode) tree.getChild(i);
            myProcess(temp);
        }

        ArrayList<Node> s= tree.getChildren();

        inputTableList.clear();
        outputTableList.clear();
        withTableList.clear();

        Map<Rule, NodeProcessor> rules = new LinkedHashMap<Rule, NodeProcessor>();

        Dispatcher disp = new DefaultRuleDispatcher(this, rules, null);
        GraphWalker ogw = new DefaultGraphWalker(disp);

        ArrayList topNodes = new ArrayList();
        topNodes.add(tree);
        ogw.startWalking(topNodes, null);
    }

    /**
     * 获取表字段类型
     * @throws ParseException
     * @throws SemanticException
     */
    public void getLineFieldInfo(String tableName, String query)throws ParseException,SemanticException{
        ParseDriver pd = new ParseDriver();
        ASTNode tree = pd.parse(query);
        while ((tree.getToken() == null) && (tree.getChildCount() > 0)) {
            tree = (ASTNode) tree.getChild(0);
        }

        ArrayList<Node> s= tree.getChildren();

        inputTableList.clear();
        outputTableList.clear();
        withTableList.clear();

        Map<Rule, NodeProcessor> rules = new LinkedHashMap<Rule, NodeProcessor>();

        Dispatcher disp = new DefaultRuleDispatcher(this, rules, null);
        GraphWalker ogw = new DefaultGraphWalker(disp);

        ArrayList topNodes = new ArrayList();
        topNodes.add(tree);
        ogw.startWalking(topNodes, null);
        //写入到表中
    }
//    public static void main(String[] args) throws IOException, ParseException, SemanticException {
//        String query="with\n" +
//                "a as (select userid user_id,messagename message_name,dt,text text from dws_bi.message_callback_userlist where dt >= from_unixtime(unix_timestamp('${dt}','yyyyMMdd') - 2*86400,'yyyyMMdd') and dt <= '${dt}' group by userid,messagename,text,dt),\n" +
//                "b as (select b.user_id,b.dt recall_day,collect_set(concat_ws('/',a.message_name,a.text)) recall_tag from a,\n" +
//                "(select b.user_id,b.dt from dws_bi.client_log_funnel b where b.dt >= from_unixtime(unix_timestamp('${dt}','yyyyMMdd') - 2*86400,'yyyyMMdd') and b.dt < '${dt}' group by b.user_id,b.dt\n" +
//                "union all\n" +
//                "select b.userid user_id,b.dt from dwd_vvmusic.client_log_common_topage b where b.dt = '${dt}' and lower(to_page) = to_page and lower(from_page) = from_page and length(device_id) > 10 group by b.dt,b.userid) b\n" +
//                "where b.dt >= a.dt and a.user_id = b.user_id group by b.user_id,b.dt)\n" +
//                "insert overwrite table dws_base.user_recall_data\n" +
//                "select min(recall_day) recall_day,user_id,recall_tag\n" +
//                "from\n" +
//                "(select recall_day,cast(user_id as bigint) user_id,recall_tag from b\n" +
//                "union all\n" +
//                "select recall_day,user_id,recall_tag from dws_base.user_recall_data)a\n" +
//                "group by user_id,recall_tag";
//        //   String query="insert overwrite table dws_base.user_message partition(dt) select * from dws_base.user_message where dt = '${dt}'";
//        SQLLineageUtils lep = new SQLLineageUtils();
//        lep.getLineageInfo(query);
//        System.out.println("Input tables = " + lep.getInputTableList());
//        System.out.println("Output tables = " + lep.getOutputTableList());
//        System.out.println("with tables = " + lep.getWithTableList());
//        /**linkTable*/
//        System.out.println("output tables line ="+lep.getTableLineList());
//        if (lep.getOutputTableList().size()==0) System.out.println(lep.getOutputTableList());
//        for (String t:lep.asMap.keySet()) {
//            //表名
//            String str= lep.asMap.get(t).first.toString();
//            //字段
//            String str1= lep.asMap.get(t).second.toString();
//            //条件字段
//            String str2= lep.asMap.get(t).third.toString();
//            //表条件
//            String str3= lep.asMap.get(t).four.toString();
//
//            System.out.println(t+" as "+str+","+str1+","+str2+","+str3);
//        }
//    }
}
