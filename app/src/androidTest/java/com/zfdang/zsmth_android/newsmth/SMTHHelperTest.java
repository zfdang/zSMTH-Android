package com.zfdang.zsmth_android.newsmth;

import com.zfdang.zsmth_android.models.Topic;

import junit.framework.TestCase;

import java.util.List;

/**
 * Created by zfdang on 2016-3-17.
 */
public class SMTHHelperTest extends TestCase {

    final String sGuidanceResponse = "\n" +
            "<html>\n" +
            "<head>\n" +
            "<title>mainpage</title>\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=gb2312\">\n" +
            "<script src=\"static/www2-main.js\"></script>\n" +
            "<script type=\"text/javascript\">writeCssMainpage();</script>\n" +
            "</head>\n" +
            "<body leftmargin=\"5\" topmargin=\"0\" marginwidth=\"0\" marginheight=\"0\">\n" +
            "<script src=\"images/randomad.js\" type=\"text/javascript\"></script>\n" +
            "<table><tr><td valign=top width=154><img id=\"bottom\"  src=\"images/ad/1.gif\" onload=notifyLoad(this.src); name=bottom></td></tr></table>\n" +
            "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
            "  <form action=\"bbssel.php\">\n" +
            "  <tr> \n" +
            "    <td height=\"18\" width=\"84\" class=\"header\" align=\"center\"><a href=\"bbsdoc.php?board=Announce\">系统公告</a></td>\n" +
            "    <td width=\"84\" class=\"header\" align=\"center\"><a href=\"bbsrecommend.php\">推荐文章</a></td>\n" +
            "    <td width=\"100\" class=\"header\" align=\"center\"><a href=\"bbsfav.php?x\">分类讨论区</a></td>\n" +
            "    <!--<td width=\"80\" class=\"header\" align=\"center\"><a href=\"bbsxmlbrd.php?flag=0\">推荐版面</a></td>-->\n" +
            "    <td width=\"80\" class=\"header\" align=\"center\"><a href=\"bbsfav.php?select=49&x\">推荐版面</a></td>\n" +
            "    <td width=\"81\" class=\"header\" align=\"center\"><a href=\"bbsxmlbrd.php?flag=1\">人气排名</a></td>\n" +
            "    <td width=\"79\" class=\"header\" align=\"center\"><a href=\"#todaybless\">本日祝福</a></td>\n" +
            "    <td width=\"79\" class=\"header\" align=\"center\">\n" +
            "    <a href=\"/pc/pcmain.php\">水木BLOG</a>\n" +
            "    </td>\n" +
            "\t<td width=\"79\" class=\"header\" align=\"center\"><a href=\"help.html\">帮助与说明</a></td>\n" +
            "\t<td width=\"79\" class=\"header\" align=\"center\"><a href=\"bbsnotice.php?file=hotinfo\">近期热点</a></td>\n" +
            "\t<td width=\"79\" class=\"header\" align=\"center\"><a href=\"bbsnotice.php?file=systeminfo\">系统热点</a></td>\n" +
            "    <td class=\"header\"></td>\n" +
            "    <td class=\"header\" align=\"right\" width=\"315\"> <input type=\"text\" name=\"board\" size=\"12\" maxlength=\"30\" value=\"版面搜索\" class=\"text\"> \n" +
            "      <input type=\"submit\" size=\"15\" value=\"GO\" class=\"button\"> \n" +
            "    </td>\n" +
            "  </tr></form>\n" +
            "</table>\n" +
            "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
            "  <tr>\n" +
            "    <td colspan=\"5\" height=\"8\"></td>\n" +
            "  </tr>\n" +
            "  <tr>\n" +
            "    <td width=\"80%\" valign=\"top\">\n" +
            "\t<div align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" class=\"recommend_title\">\n" +
            "        <tr> \n" +
            "          <td>&nbsp;&nbsp;推荐文章</td>\n" +
            "\t\t          </tr>\n" +
            "\t</table></div>\n" +
            "\n" +
            "\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"RecommendTable\" align=\"center\">\n" +
            "\t<tr><td height=10 colspan=2></td></tr>\n" +
            "<tr>\n" +
            "<td class=\"RecommendTitle\"><script type=\"text/javascript\">putImage(\"recommend.gif\",\"\");</script>&nbsp;<a href=\"bbsrecon.php?id=19406\">好久没来了，补个总结。。。haha </a></td><td class=\"RecommendLink\"><div align=\"right\">[<a href=\"bbsdoc.php?board=PetsEden\">宠物乐园</a>] [<a href=\"bbstcon.php?board=PetsEden&gid=1248788\">同主题阅读原文</a>]</div>\n" +
            "</td></tr><tr><td colspan=2><dl style=\"MARGIN-TOP: 1px;MARGIN-BOTTOM: 5px; MARGIN-LEFT: 25px;\"><dt>\n" +
            "本来年底想写来着，一晃又俩月了。。。\n" +
            "去年最让人开心的变化就是Lima小朋友彻底变粘人乖喵了，要提出来单写！去年4月时候写过一则lima的变化，那时候发现她已经不再暴力对我们多了很多信任，反省猫奴我也该敞开心扉接纳这个新形象，不要再防着她。这下可好 </dl>\n" +
            "</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "<td class=\"RecommendTitle\"><script type=\"text/javascript\">putImage(\"recommend.gif\",\"\");</script>&nbsp;<a href=\"bbsrecon.php?id=19410\">不合格党员的思想汇报 </a></td><td class=\"RecommendLink\"><div align=\"right\">[<a href=\"bbsdoc.php?board=CouponsLife\">辣妈羊毛党</a>] [<a href=\"bbstcon.php?board=CouponsLife&gid=2309741\">同主题阅读原文</a>]</div>\n" +
            "</td></tr><tr><td colspan=2><dl style=\"MARGIN-TOP: 1px;MARGIN-BOTTOM: 5px; MARGIN-LEFT: 25px;\"><dt>\n" +
            "结识毛版二年有余，跟着大伙儿薅过好奇，薅过帮宝适，薅过唯品会，薅过亚麻，薅过银联钱包，也薅了猴子卡，薅过各种淘宝的微信的红包，各种扫码摇一摇……从最初的买东西懂得看看有没有券可领，到学会了拆单，用返利网，搞零元单，一路走来，心得不敢说，感 </dl>\n" +
            "</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "<td class=\"RecommendTitle\"><script type=\"text/javascript\">putImage(\"recommend.gif\",\"\");</script>&nbsp;<a href=\"bbsrecon.php?id=19418\">想当年在北工大的日子(你们说是几就是几,我记不得了) </a></td><td class=\"RecommendLink\"><div align=\"right\">[<a href=\"bbsdoc.php?board=BJUT\">北京工业大学</a>] [<a href=\"bbstcon.php?board=BJUT&gid=26344\">同主题阅读原文</a>]</div>\n" +
            "</td></tr><tr><td colspan=2><dl style=\"MARGIN-TOP: 1px;MARGIN-BOTTOM: 5px; MARGIN-LEFT: 25px;\"><dt>\n" +
            "停了好久,前一阵子实在起不来床,这一阵子好点了,慢慢捡起来吧.不过就我这种严重的拖延症患者,估计老二生出来了也写不完.好在文字上下本来就没什么联系.\n" +
            "上次写到哪里来着?\n" +
            "老公问我为啥不写点学习上的事。是呀，老是听人说&quot;学生的首要任务是学习&quot;，可是回想 </dl>\n" +
            "</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "<td class=\"RecommendTitle\"><script type=\"text/javascript\">putImage(\"recommend.gif\",\"\");</script>&nbsp;<a href=\"bbsrecon.php?id=19438\">[原创]中兴事件之痛--谁扒掉了中国电子整机产业的皇帝新衣 </a></td><td class=\"RecommendLink\"><div align=\"right\">[<a href=\"bbsdoc.php?board=METech\">微电子技术</a>] [<a href=\"bbstcon.php?board=METech&gid=307228\">同主题阅读原文</a>]</div>\n" +
            "</td></tr><tr><td colspan=2><dl style=\"MARGIN-TOP: 1px;MARGIN-BOTTOM: 5px; MARGIN-LEFT: 25px;\"><dt>\n" +
            "中兴事件之痛 ——谁扒掉了中国电子整机产业的皇帝新衣\n" +
            "3月7日早上，一个朋友给笔者打电话：“中兴停牌你知道不？美国政府禁止中兴采购了”。此时笔者的注意力还集中在今年女生节新出的条幅上，不以为然的答道：&quot;看到报道了，估计美国政府也就做做样子吧&quot;。 </dl>\n" +
            "</td>\n" +
            "</tr>\n" +
            "<tr>\n" +
            "<td class=\"RecommendTitle\"><script type=\"text/javascript\">putImage(\"recommend.gif\",\"\");</script>&nbsp;<a href=\"bbsrecon.php?id=19433\">我所经历的江南 学堂篇 </a></td><td class=\"RecommendLink\"><div align=\"right\">[<a href=\"bbsdoc.php?board=Nostalgia\">怀旧文化</a>] [<a href=\"bbstcon.php?board=Nostalgia&gid=20669\">同主题阅读原文</a>]</div>\n" +
            "</td></tr><tr><td colspan=2><dl style=\"MARGIN-TOP: 1px;MARGIN-BOTTOM: 5px; MARGIN-LEFT: 25px;\"><dt>\n" +
            "多年以后，在北京西北三环那所著名学府的大草坪上等着扔学位帽的时候，时任学士候选\n" +
            "人楼某某回想起小学校长带他们去拍整个学校历史上唯一一张集体照的遥远的春天的下\n" +
            "午。那时的石珠坞小学还在全盛时期，是个有着一位校长，一位老师，四个年级，二十多\n" +
            "位学 </dl>\n" +
            "</td>\n" +
            "</tr>\n" +
            "\n" +
            "<tr><td width=\"100%\" height=25 align=\"right\" colspan=2><a href=\"bbsrecommend.php\">更多推荐文章</a></td></tr>\n" +
            "\t</table>\n" +
            "\n" +
            "<br>\n" +
            "<script async src=\"//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js\"></script>\n" +
            "<ins class=\"adsbygoogle\"\n" +
            "     style=\"display:inline-block;width:728px;height:90px\"\n" +
            "     data-ad-client=\"ca-pub-4332125961076003\"\n" +
            "     data-ad-slot=\"2397407686\"></ins>\n" +
            "<script>\n" +
            "(adsbygoogle = window.adsbygoogle || []).push({});\n" +
            "</script>\n" +
            "<!--\n" +
            "<script type=\"text/javascript\">\n" +
            "    var cpro_id = \"u2185385\";\n" +
            "</script>\n" +
            "<script src=\"http://cpro.baidustatic.com/cpro/ui/c.js\" type=\"text/javascript\"></script>\n" +
            "-->\n" +
            "<br>\n" +
            "\t<div align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" class=\"hot_title\">\n" +
            "        <tr> \n" +
            "          <td>&nbsp;&nbsp;本日热点话题讨论 [<a href=\"rssi.php?h=1\" target=\"_blank\">RSS</a>]</td>\n" +
            "\t\t          </tr>\n" +
            "\t</table></div>\n" +
            "\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"HotTable\" align=\"center\">\n" +
            "\t<tr><td class=\"HotTitle\">\n" +
            "[<a href=\"bbsdoc.php?board=SchoolEstate\">学区房</a>]\n" +
            "<a href=\"bbstcon.php?board=SchoolEstate&gid=418849\">关于学区房，死活理解不了 </a></td><td class=\"HotAuthor\"><a href=\"bbsqry.php?userid=liuyulin8309\">liuyulin8309</a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"HotTitle\">\n" +
            "[<a href=\"bbsdoc.php?board=Occupier\">业主之家</a>]\n" +
            "<a href=\"bbstcon.php?board=Occupier&gid=948242\">我有一间屋，足以慰生苦。 </a></td><td class=\"HotAuthor\"><a href=\"bbsqry.php?userid=airys\">airys</a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"HotTitle\">\n" +
            "[<a href=\"bbsdoc.php?board=Divorce\">离婚</a>]\n" +
            "<a href=\"bbstcon.php?board=Divorce&gid=560613\">ex快疯了，开始攻击我现在的老婆了 </a></td><td class=\"HotAuthor\"><a href=\"bbsqry.php?userid=wodeheiye\">wodeheiye</a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"HotTitle\">\n" +
            "[<a href=\"bbsdoc.php?board=FamilyLife\">家庭生活</a>]\n" +
            "<a href=\"bbstcon.php?board=FamilyLife&gid=1757783669\">老公赚多少，各位太太们就考虑事业上不那么拼了？ </a></td><td class=\"HotAuthor\"><a href=\"bbsqry.php?userid=qingtianwan\">qingtianwan</a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"HotTitle\">\n" +
            "[<a href=\"bbsdoc.php?board=Picture\">贴图</a>]\n" +
            "<a href=\"bbstcon.php?board=Picture&gid=1174346\">那些电视剧里的逆天台词 </a></td><td class=\"HotAuthor\"><a href=\"bbsqry.php?userid=zhuxianjian\">zhuxianjian</a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"HotTitle\">\n" +
            "[<a href=\"bbsdoc.php?board=ADAgent_TG\">团购</a>]\n" +
            "<a href=\"bbstcon.php?board=ADAgent_TG&gid=1068085\">[团购]3.17-3.23海南大金煌芒 贵妃芒 9.9秒杀户太8号原浆葡萄酒 </a></td><td class=\"HotAuthor\"><a href=\"bbsqry.php?userid=lipingy119\">lipingy119</a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"HotTitle\">\n" +
            "[<a href=\"bbsdoc.php?board=Age\">大龄男女</a>]\n" +
            "<a href=\"bbstcon.php?board=Age&gid=16008983\">真心喜欢一个人是什么样的体验？ </a></td><td class=\"HotAuthor\"><a href=\"bbsqry.php?userid=xiaolilly\">xiaolilly</a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"HotTitle\">\n" +
            "[<a href=\"bbsdoc.php?board=AutoWorld\">汽车世界</a>]\n" +
            "<a href=\"bbstcon.php?board=AutoWorld&gid=1939754236\">怎么劝邻居买安全座椅 </a></td><td class=\"HotAuthor\"><a href=\"bbsqry.php?userid=nicolexy\">nicolexy</a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"HotTitle\">\n" +
            "[<a href=\"bbsdoc.php?board=OurEstate\">二手房交流</a>]\n" +
            "<a href=\"bbstcon.php?board=OurEstate&gid=1664288\">最近房价涨的有点离谱了不，去年9月买的，现在涨了100w? </a></td><td class=\"HotAuthor\"><a href=\"bbsqry.php?userid=lkdsgrjra\">lkdsgrjra</a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"HotTitle\">\n" +
            "[<a href=\"bbsdoc.php?board=MyPhoto\">个人Show</a>]\n" +
            "<a href=\"bbstcon.php?board=MyPhoto&gid=2123227\">新人报道，勿拍，多多支持给分。 </a></td><td class=\"HotAuthor\"><a href=\"bbsqry.php?userid=candy90\">candy90</a>&nbsp;&nbsp;</td></tr>\n" +
            "\t\t</table>\n" +
            "\t\t<div align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" class=\"type_title\">\n" +
            "        <tr> \n" +
            "          <td>&nbsp;&nbsp;分类精彩讨论区</td>\n" +
            "\t\t          </tr>\n" +
            "\t</table></div>\n" +
            "\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"SecTable\" align=\"center\">\n" +
            "\t\t  <tr> \n" +
            "  <td valign=\"top\" class=\"SectionTitle\"><script type=\"text/javascript\">putImage(\"section.gif\",\"\");</script>&nbsp;<span class=\"SectionName\"><a href=\"bbsboa.php?group=1\">国内院校</a></span>&nbsp;&nbsp;<span class=\"SectionList\">\n" +
            "<a href=\"bbsdoc.php?board=HUST\">华中科技大学</a>, \n" +
            "<a href=\"bbsdoc.php?board=CAS\">中国科学院</a>, \n" +
            "<a href=\"bbsdoc.php?board=HIT\">哈尔滨工业大学</a>, \n" +
            "<a href=\"bbsdoc.php?board=GaoKao\">高考·大学</a>, \n" +
            "<a href=\"bbsdoc.php?board=ZJU\">浙江大学</a>, \n" +
            "<a href=\"rssi.php?h=2&s=1\" target=\"_blank\">[RSS]</a>\n" +
            "<a href=\"bbsboa.php?group=1\">[更多]</a></span></td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=CAS\">中国科学院</a>]\n" +
            "<a href=\"bbstcon.php?board=CAS&gid=136281\">[讨论]谁能给科普一下zg改进人们生活的发明。 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=BJUT\">北京工业大学</a>]\n" +
            "<a href=\"bbstcon.php?board=BJUT&gid=33600\">我们单位疯了 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=ZJU\">浙江大学</a>]\n" +
            "<a href=\"bbstcon.php?board=ZJU&gid=904067\">o0 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=HUST\">华中科技大学</a>]\n" +
            "<a href=\"bbstcon.php?board=HUST&gid=77056\">李一男 内幕交易被抓 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=NCUT\">北方工业大学</a>]\n" +
            "<a href=\"bbstcon.php?board=NCUT&gid=19724\">哎╯﹏╰ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=HIT\">哈尔滨工业大学</a>]\n" +
            "<a href=\"bbstcon.php?board=HIT&gid=118659\">哈工大和北航比，师资队伍最近10年落后了 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=BIT\">北京理工大学</a>]\n" +
            "<a href=\"bbstcon.php?board=BIT&gid=250034\">能有人送我一个北理ftp联盟的账号吗，谢谢 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=THAXiqu\">戏曲类社团</a>]\n" +
            "<a href=\"bbstcon.php?board=THAXiqu&gid=49745\">关于王老演唱会的一个脑洞 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=DEE.THU\">清华电子工程系</a>]\n" +
            "<a href=\"bbstcon.php?board=DEE.THU&gid=247565\">找一个英翻中翻译，电子信息类，报酬可观 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=GaoKao\">高考·大学</a>]\n" +
            "<a href=\"bbstcon.php?board=GaoKao&gid=309398\">为上名校高考志愿服从调剂 毕业月薪只有4000块 </a>&nbsp;&nbsp;</td></tr>\n" +
            "\n" +
            "</tr>\n" +
            "        <tr> \n" +
            "          <td height=\"1\" class=\"SecLine\"></td>\n" +
            "        </tr>\n" +
            "<tr> \n" +
            "  <td valign=\"top\" class=\"SectionTitle\"><script type=\"text/javascript\">putImage(\"section.gif\",\"\");</script>&nbsp;<span class=\"SectionName\"><a href=\"bbsboa.php?group=2\">休闲娱乐</a></span>&nbsp;&nbsp;<span class=\"SectionList\">\n" +
            "<a href=\"bbsdoc.php?board=Picture\">贴图</a>, \n" +
            "<a href=\"bbsdoc.php?board=Joke\">笑话连篇</a>, \n" +
            "<a href=\"bbsdoc.php?board=MyPhoto\">个人Show</a>, \n" +
            "<a href=\"bbsdoc.php?board=Movie\">电影</a>, \n" +
            "<a href=\"bbsdoc.php?board=TVShow\">电视秀</a>, \n" +
            "<a href=\"rssi.php?h=2&s=2\" target=\"_blank\">[RSS]</a>\n" +
            "<a href=\"bbsboa.php?group=2\">[更多]</a></span></td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Joke\">笑话连篇</a>]\n" +
            "<a href=\"bbstcon.php?board=Joke&gid=3548255\">牛娇客（一）zz </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=MyPhoto\">个人Show</a>]\n" +
            "<a href=\"bbstcon.php?board=MyPhoto&gid=2123367\">A4腰，终于成功了 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Picture\">贴图</a>]\n" +
            "<a href=\"bbstcon.php?board=Picture&gid=1174702\">作为孩子爸，看到这张照片，还是忍不住掉泪了 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=TVShow\">电视秀</a>]\n" +
            "<a href=\"bbstcon.php?board=TVShow&gid=1232140\">最强大脑林建东好怂啊 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=TV\">电视</a>]\n" +
            "<a href=\"bbstcon.php?board=TV&gid=1050600\">太阳的后裔男主一般吧 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Movie\">电影</a>]\n" +
            "<a href=\"bbstcon.php?board=Movie&gid=2630676\">《大王叫我来巡山》够得上神曲级别吗？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=PetsEden\">宠物乐园</a>]\n" +
            "<a href=\"bbstcon.php?board=PetsEden&gid=1250210\">说不定，它会成为世界最美萨摩耶 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Jump\">Jump系漫画</a>]\n" +
            "<a href=\"bbstcon.php?board=Jump&gid=136390\">op820 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=MMJoke\">幽默全方位</a>]\n" +
            "<a href=\"bbstcon.php?board=MMJoke&gid=1634776468\">对单身狗造成一万点伤害 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=OMTV\">欧美电视</a>]\n" +
            "<a href=\"bbstcon.php?board=OMTV&gid=565768\">无耻之徒实在看不下去了 </a>&nbsp;&nbsp;</td></tr>\n" +
            "\n" +
            "</tr>\n" +
            "        <tr> \n" +
            "          <td height=\"1\" class=\"SecLine\"></td>\n" +
            "        </tr>\n" +
            "<tr> \n" +
            "  <td valign=\"top\" class=\"SectionTitle\"><script type=\"text/javascript\">putImage(\"section.gif\",\"\");</script>&nbsp;<span class=\"SectionName\"><a href=\"bbsboa.php?group=3\">五湖四海</a></span>&nbsp;&nbsp;<span class=\"SectionList\">\n" +
            "<a href=\"bbsdoc.php?board=Shanghai\">上海滩</a>, \n" +
            "<a href=\"bbsdoc.php?board=HuiLongGuan\">回龙观</a>, \n" +
            "<a href=\"bbsdoc.php?board=Sichuan\">天府之国·四川</a>, \n" +
            "<a href=\"bbsdoc.php?board=Hubei\">极目楚天·湖北</a>, \n" +
            "<a href=\"bbsdoc.php?board=Canada\">枫之国</a>, \n" +
            "<a href=\"rssi.php?h=2&s=3\" target=\"_blank\">[RSS]</a>\n" +
            "<a href=\"bbsboa.php?group=3\">[更多]</a></span></td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Guangdong\">南粤风情·广东</a>]\n" +
            "<a href=\"bbstcon.php?board=Guangdong&gid=852255\">广州天河三大板块 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Sichuan\">天府之国·四川</a>]\n" +
            "<a href=\"bbstcon.php?board=Sichuan&gid=558585\">重庆房价比成都便宜？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Zhejiang\">诗画之江·浙江</a>]\n" +
            "<a href=\"bbstcon.php?board=Zhejiang&gid=217312\">杭州学区房PK，保叔塔9年一贯制和文三街本部。 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Henan\">中原风采·河南</a>]\n" +
            "<a href=\"bbstcon.php?board=Henan&gid=514314\">伊人赏春 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Shenzhen\">深圳特区</a>]\n" +
            "<a href=\"bbstcon.php?board=Shenzhen&gid=66836\">其实我有些恐慌... </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=HuiLongGuan\">回龙观</a>]\n" +
            "<a href=\"bbstcon.php?board=HuiLongGuan&gid=1634547208\">我找到西二旗北路上堵死的根源了 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Hubei\">极目楚天·湖北</a>]\n" +
            "<a href=\"bbstcon.php?board=Hubei&gid=417272\">想回武汉买套房 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Tianjin\">天津卫</a>]\n" +
            "<a href=\"bbstcon.php?board=Tianjin&gid=288296\">谈谈到天津（滨海新区）生活一年的感受 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Liaoning\">关东龙睛·辽宁</a>]\n" +
            "<a href=\"bbstcon.php?board=Liaoning&gid=233362\">这回在四川打架打得好！ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Shanghai\">上海滩</a>]\n" +
            "<a href=\"bbstcon.php?board=Shanghai&gid=1869206311\">征男友，88年萌妹纸，是吃货又是小厨娘，热爱旅行 </a>&nbsp;&nbsp;</td></tr>\n" +
            "\n" +
            "</tr>\n" +
            "        <tr> \n" +
            "          <td height=\"1\" class=\"SecLine\"></td>\n" +
            "        </tr>\n" +
            "<tr> \n" +
            "  <td valign=\"top\" class=\"SectionTitle\"><script type=\"text/javascript\">putImage(\"section.gif\",\"\");</script>&nbsp;<span class=\"SectionName\"><a href=\"bbsboa.php?group=4\">游戏运动</a></span>&nbsp;&nbsp;<span class=\"SectionList\">\n" +
            "<a href=\"bbsdoc.php?board=Weiqi\">纹枰论道</a>, \n" +
            "<a href=\"bbsdoc.php?board=Football\">绿茵世界</a>, \n" +
            "<a href=\"bbsdoc.php?board=BasketballForum\">篮球</a>, \n" +
            "<a href=\"bbsdoc.php?board=WorldSoccer\">国际足球</a>, \n" +
            "<a href=\"bbsdoc.php?board=Travel\">旅游</a>, \n" +
            "<a href=\"rssi.php?h=2&s=4\" target=\"_blank\">[RSS]</a>\n" +
            "<a href=\"bbsboa.php?group=4\">[更多]</a></span></td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=BasketballForum\">篮球</a>]\n" +
            "<a href=\"bbstcon.php?board=BasketballForum&gid=3763093\">最完整起因出来了，四川球迷围攻红色衣服引起冲突！ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=WorldSoccer\">国际足球</a>]\n" +
            "<a href=\"bbstcon.php?board=WorldSoccer&gid=16654105\">铁血真球迷打卡了！ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Weiqi\">纹枰论道</a>]\n" +
            "<a href=\"bbstcon.php?board=Weiqi&gid=552789\">大家说说真的会让自己孩子可以去学习下围棋吗？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Football\">绿茵世界</a>]\n" +
            "<a href=\"bbstcon.php?board=Football&gid=2741715\">中国和韩国俱乐部到底差在哪呢 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Travel\">旅游</a>]\n" +
            "<a href=\"bbstcon.php?board=Travel&gid=388533\">美媒：热水是中国人“最好的饮料”zz </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Cyclone\">自行车运动</a>]\n" +
            "<a href=\"bbstcon.php?board=Cyclone&gid=901872\">小米自行车众筹宣传页面 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=RunningLife\">跑道人生</a>]\n" +
            "<a href=\"bbstcon.php?board=RunningLife&gid=686845\">为了tiffany，2016名古屋女子马拉松 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Bundesliga\">德国足球</a>]\n" +
            "<a href=\"bbstcon.php?board=Bundesliga&gid=75539\">瓜差点要被黑成碳 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=LoseFat\">健康减肥</a>]\n" +
            "<a href=\"bbstcon.php?board=LoseFat&gid=491177\">[求助]遇到平台期。。。应该怎么往下减呢？162,70KG【女】 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=WoW\">魔兽世界</a>]\n" +
            "<a href=\"bbstcon.php?board=WoW&gid=2678623\">现在的世界观里，就4个上古之神？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "\n" +
            "</tr>\n" +
            "        <tr> \n" +
            "          <td height=\"1\" class=\"SecLine\"></td>\n" +
            "        </tr>\n" +
            "<tr> \n" +
            "  <td valign=\"top\" class=\"SectionTitle\"><script type=\"text/javascript\">putImage(\"section.gif\",\"\");</script>&nbsp;<span class=\"SectionName\"><a href=\"bbsboa.php?group=5\">社会信息</a></span>&nbsp;&nbsp;<span class=\"SectionList\">\n" +
            "<a href=\"bbsdoc.php?board=RealEstate\">房地产论坛</a>, \n" +
            "<a href=\"bbsdoc.php?board=OurEstate\">二手房交流</a>, \n" +
            "<a href=\"bbsdoc.php?board=EconForum\">经济论坛</a>, \n" +
            "<a href=\"bbsdoc.php?board=Occupier\">业主之家</a>, \n" +
            "<a href=\"bbsdoc.php?board=Stock\">股市</a>, \n" +
            "<a href=\"rssi.php?h=2&s=5\" target=\"_blank\">[RSS]</a>\n" +
            "<a href=\"bbsboa.php?group=5\">[更多]</a></span></td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=RealEstate\">房地产论坛</a>]\n" +
            "<a href=\"bbstcon.php?board=RealEstate&gid=4987212\">我们将见证历史 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=ShangHaiEstate\">上海房地产</a>]\n" +
            "<a href=\"bbstcon.php?board=ShangHaiEstate&gid=100643\">刚需求建议 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=OurEstate\">二手房交流</a>]\n" +
            "<a href=\"bbstcon.php?board=OurEstate&gid=1660049\">【求助】房主毁约，借口有精神病不肯赔偿 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=SchoolEstate\">学区房</a>]\n" +
            "<a href=\"bbstcon.php?board=SchoolEstate&gid=419221\">同事外地户口吹嘘昌平王府学校很牛逼 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Stock\">股市</a>]\n" +
            "<a href=\"bbstcon.php?board=Stock&gid=6293538\">幸好老夫用了第36计 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=ITExpress\">IT业界特快</a>]\n" +
            "<a href=\"bbstcon.php?board=ITExpress&gid=1627396\">百度：用于外卖的人工智能不比下围棋low </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=MyWallet\">金融产品及个人理财</a>]\n" +
            "<a href=\"bbstcon.php?board=MyWallet&gid=1012086\">香港二百万港币怎么弄回来 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Occupier\">业主之家</a>]\n" +
            "<a href=\"bbstcon.php?board=Occupier&gid=948980\">准备下个月开始装修，现在买东西是否早 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=SecondComputer\">二手电脑市场</a>]\n" +
            "<a href=\"bbstcon.php?board=SecondComputer&gid=1844246\">热烈庆祝组装W700ds控制性工程竣工！ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=IPO\">新股</a>]\n" +
            "<a href=\"bbstcon.php?board=IPO&gid=65394\">中了一签名家汇 </a>&nbsp;&nbsp;</td></tr>\n" +
            "\n" +
            "</tr>\n" +
            "        <tr> \n" +
            "          <td height=\"1\" class=\"SecLine\"></td>\n" +
            "        </tr>\n" +
            "<tr> \n" +
            "  <td valign=\"top\" class=\"SectionTitle\"><script type=\"text/javascript\">putImage(\"section.gif\",\"\");</script>&nbsp;<span class=\"SectionName\"><a href=\"bbsboa.php?group=6\">知性感性</a></span>&nbsp;&nbsp;<span class=\"SectionList\">\n" +
            "<a href=\"bbsdoc.php?board=FamilyLife\">家庭生活</a>, \n" +
            "<a href=\"bbsdoc.php?board=CouponsLife\">辣妈羊毛党</a>, \n" +
            "<a href=\"bbsdoc.php?board=AutoWorld\">汽车世界</a>, \n" +
            "<a href=\"bbsdoc.php?board=WorkLife\">职业生涯</a>, \n" +
            "<a href=\"bbsdoc.php?board=Age\">大龄男女</a>, \n" +
            "<a href=\"rssi.php?h=2&s=6\" target=\"_blank\">[RSS]</a>\n" +
            "<a href=\"bbsboa.php?group=6\">[更多]</a></span></td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=FamilyLife\">家庭生活</a>]\n" +
            "<a href=\"bbstcon.php?board=FamilyLife&gid=1757783676\">闺蜜来上海玩，我是否应该承担她旅游的费用？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=AutoWorld\">汽车世界</a>]\n" +
            "<a href=\"bbstcon.php?board=AutoWorld&gid=1939754300\">整天被版上忽悠3.0雅阁，终于要出手了。 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=WorkLife\">职业生涯</a>]\n" +
            "<a href=\"bbstcon.php?board=WorkLife&gid=1108624\">李一男算不算屌丝男 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Children\">孩子</a>]\n" +
            "<a href=\"bbstcon.php?board=Children&gid=931536942\">求助：媳妇产后奶水不足，麻烦大家过来人给支下招吧 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Age\">大龄男女</a>]\n" +
            "<a href=\"bbstcon.php?board=Age&gid=16009356\">如果找不到合适的人，最重要的还是自己过得好 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=CouponsLife\">辣妈羊毛党</a>]\n" +
            "<a href=\"bbstcon.php?board=CouponsLife&gid=2327667\">京东全品200-20.500-40 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Universal\">特快万象</a>]\n" +
            "<a href=\"bbstcon.php?board=Universal&gid=677494\">提丝不是以前的提丝了 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Pregnancy\">怀孕</a>]\n" +
            "<a href=\"bbstcon.php?board=Pregnancy&gid=1011842\">汇报一下，我生完了！ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Divorce\">离婚</a>]\n" +
            "<a href=\"bbstcon.php?board=Divorce&gid=558535\">[建议][求助]老公嫌弃我挣钱少，吵着离婚 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Oversea\">海外学人</a>]\n" +
            "<a href=\"bbstcon.php?board=Oversea&gid=3938131\">美帝媒体屁股好明显各种黑破床 </a>&nbsp;&nbsp;</td></tr>\n" +
            "\n" +
            "</tr>\n" +
            "        <tr> \n" +
            "          <td height=\"1\" class=\"SecLine\"></td>\n" +
            "        </tr>\n" +
            "<tr> \n" +
            "  <td valign=\"top\" class=\"SectionTitle\"><script type=\"text/javascript\">putImage(\"section.gif\",\"\");</script>&nbsp;<span class=\"SectionName\"><a href=\"bbsboa.php?group=7\">文化人文</a></span>&nbsp;&nbsp;<span class=\"SectionList\">\n" +
            "<a href=\"bbsdoc.php?board=History\">历史</a>, \n" +
            "<a href=\"bbsdoc.php?board=Marvel\">聊斋鬼话</a>, \n" +
            "<a href=\"bbsdoc.php?board=FairCity\">锦绣都·言情</a>, \n" +
            "<a href=\"bbsdoc.php?board=NetNovel\">网络小说</a>, \n" +
            "<a href=\"bbsdoc.php?board=Railway\">铁路</a>, \n" +
            "<a href=\"rssi.php?h=2&s=7\" target=\"_blank\">[RSS]</a>\n" +
            "<a href=\"bbsboa.php?group=7\">[更多]</a></span></td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=SF\">科学幻想</a>]\n" +
            "<a href=\"bbstcon.php?board=SF&gid=402646\">穿越回1千多年前做皇帝，依靠当时的生产力，可以生产些什么？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Law\">法学与法律</a>]\n" +
            "<a href=\"bbstcon.php?board=Law&gid=825610873\">阳光权被部队的树侵犯，我们该怎么办，求大神指点迷津 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=History\">历史</a>]\n" +
            "<a href=\"bbstcon.php?board=History&gid=1567225\">波兰人说他们国家两边各一个日本 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Emprise\">武侠世家</a>]\n" +
            "<a href=\"bbstcon.php?board=Emprise&gid=230381\">MMA，K1这些搏击高手和重量级拳击比哪个厉害？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Sanguo\">煮酒论英雄</a>]\n" +
            "<a href=\"bbstcon.php?board=Sanguo&gid=423984\">话说要是司马懿没活那么长，或者中间曹爽把他害死了，会怎么样 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=OpenIching\">开放易学</a>]\n" +
            "<a href=\"bbstcon.php?board=OpenIching&gid=392170\">开个预测帖，每日三卦 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=NetNovel\">网络小说</a>]\n" +
            "<a href=\"bbstcon.php?board=NetNovel&gid=229318\">草清这样的仙草为何传播不广？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=StoneStory\">红楼梦</a>]\n" +
            "<a href=\"bbstcon.php?board=StoneStory&gid=277218\">没见过这种色中饿鬼 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Buddha\">居士林</a>]\n" +
            "<a href=\"bbstcon.php?board=Buddha&gid=105651\">佛与罗汉的神通差异 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=FairCity\">锦绣都·言情</a>]\n" +
            "<a href=\"bbstcon.php?board=FairCity&gid=136564\">[看文日记-BL]《过门》Priest </a>&nbsp;&nbsp;</td></tr>\n" +
            "\n" +
            "</tr>\n" +
            "        <tr> \n" +
            "          <td height=\"1\" class=\"SecLine\"></td>\n" +
            "        </tr>\n" +
            "<tr> \n" +
            "  <td valign=\"top\" class=\"SectionTitle\"><script type=\"text/javascript\">putImage(\"section.gif\",\"\");</script>&nbsp;<span class=\"SectionName\"><a href=\"bbsboa.php?group=8\">学术科学</a></span>&nbsp;&nbsp;<span class=\"SectionList\">\n" +
            "<a href=\"bbsdoc.php?board=MilitaryView\">军事瞭望</a>, \n" +
            "<a href=\"bbsdoc.php?board=ChildEducation\">儿童教育</a>, \n" +
            "<a href=\"bbsdoc.php?board=Medicine\">医药卫生</a>, \n" +
            "<a href=\"bbsdoc.php?board=METech\">微电子技术</a>, \n" +
            "<a href=\"bbsdoc.php?board=Geography\">地理</a>, \n" +
            "<a href=\"rssi.php?h=2&s=8\" target=\"_blank\">[RSS]</a>\n" +
            "<a href=\"bbsboa.php?group=8\">[更多]</a></span></td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=ChildEducation\">儿童教育</a>]\n" +
            "<a href=\"bbstcon.php?board=ChildEducation&gid=301853\">怎么提高孩子的情商？我家孩子是情商低还是有点自闭啊？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=MilitaryView\">军事瞭望</a>]\n" +
            "<a href=\"bbstcon.php?board=MilitaryView&gid=1629885\">中国在赤瓜礁建成南沙第2座机场 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=METech\">微电子技术</a>]\n" +
            "<a href=\"bbstcon.php?board=METech&gid=309513\">狼博，打脸贴来了，你删光了自己发帖也没有用，看你当初闹得笑 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=MilitaryJoke\">军苑娱乐报</a>]\n" +
            "<a href=\"bbstcon.php?board=MilitaryJoke&gid=365306\">女权的巨大进步：沙特终于承认妇女是哺乳动物了 (转载) </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=MilitaryTech\">军事科学与技术</a>]\n" +
            "<a href=\"bbstcon.php?board=MilitaryTech&gid=16455810\">心神完成第五次高速滑行试验 打开加力 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Mentality\">心理</a>]\n" +
            "<a href=\"bbstcon.php?board=Mentality&gid=145649\">想从楼上跳下去，可是又觉得没到那份上 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Medicine\">医药卫生</a>]\n" +
            "<a href=\"bbstcon.php?board=Medicine&gid=226767\">家里的婆娘咳嗽，俩周了，不见好 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=AI\">人工智能</a>]\n" +
            "<a href=\"bbstcon.php?board=AI&gid=46033\">【求问，讨论】图形化思维的人是不是不太适合编程？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Geography\">地理</a>]\n" +
            "<a href=\"bbstcon.php?board=Geography&gid=240296\">自由而又聪明的人，会选择去大连吧？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Aero\">航空航天</a>]\n" +
            "<a href=\"bbstcon.php?board=Aero&gid=293555\">世界最短航班，两机场隔海相望 (转载) </a>&nbsp;&nbsp;</td></tr>\n" +
            "\n" +
            "</tr>\n" +
            "        <tr> \n" +
            "          <td height=\"1\" class=\"SecLine\"></td>\n" +
            "        </tr>\n" +
            "<tr> \n" +
            "  <td valign=\"top\" class=\"SectionTitle\"><script type=\"text/javascript\">putImage(\"section.gif\",\"\");</script>&nbsp;<span class=\"SectionName\"><a href=\"bbsboa.php?group=9\">电脑技术</a></span>&nbsp;&nbsp;<span class=\"SectionList\">\n" +
            "<a href=\"bbsdoc.php?board=PocketLife\">掌上智能</a>, \n" +
            "<a href=\"bbsdoc.php?board=DigiHome\">数字家庭</a>, \n" +
            "<a href=\"bbsdoc.php?board=Apple\">苹果</a>, \n" +
            "<a href=\"bbsdoc.php?board=Mobile\">手机·移动通信</a>, \n" +
            "<a href=\"bbsdoc.php?board=NewSoftware\">新软件介绍</a>, \n" +
            "<a href=\"rssi.php?h=2&s=9\" target=\"_blank\">[RSS]</a>\n" +
            "<a href=\"bbsboa.php?group=9\">[更多]</a></span></td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=PocketLife\">掌上智能</a>]\n" +
            "<a href=\"bbstcon.php?board=PocketLife&gid=2191787\">s7和ip6s选哪个？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Notebook\">笔记本电脑</a>]\n" +
            "<a href=\"bbstcon.php?board=Notebook&gid=1853333\">rmbp一代即完美，thinkpad n代仍然一坨屎 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=NewSoftware\">新软件介绍</a>]\n" +
            "<a href=\"bbstcon.php?board=NewSoftware&gid=199306\">怒求360的替代品 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Apple\">苹果</a>]\n" +
            "<a href=\"bbstcon.php?board=Apple&gid=1090960\">哎，苹果默不作声自动扣了我10元。 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Mobile\">手机·移动通信</a>]\n" +
            "<a href=\"bbstcon.php?board=Mobile&gid=1663983\">有消息称移动正在关闭TDS基站？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=DigiHome\">数字家庭</a>]\n" +
            "<a href=\"bbstcon.php?board=DigiHome&gid=550480\">家里的cd怎么放啊，愁 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Xiaomi\">小米</a>]\n" +
            "<a href=\"bbstcon.php?board=Xiaomi&gid=112809\">红米note3全网通高配使用感受 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Python\">Python的自由天空</a>]\n" +
            "<a href=\"bbstcon.php?board=Python&gid=129678\">pgsql比mysql好很多吗？ </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=Programming\">编程技术</a>]\n" +
            "<a href=\"bbstcon.php?board=Programming&gid=117322\">如何稳妥地截断多字节编码字符串，无需解码 </a>&nbsp;&nbsp;</td></tr>\n" +
            "<tr><td class=\"SectionItem\">[<a href=\"bbsdoc.php?board=ZTE_HW\">中兴华为</a>]\n" +
            "<a href=\"bbstcon.php?board=ZTE_HW&gid=45071\">中兴Blade A1果然是神机 </a>&nbsp;&nbsp;</td></tr>\n" +
            "\n" +
            "</tr>\n" +
            "        <tr> \n" +
            "          <td height=\"1\" class=\"SecLine\"></td>\n" +
            "        </tr>\n" +
            "      </table>\n" +
            "</td>\n" +
            "    <td width=\"1\" class=\"vline\"></td>\n" +
            "    <td width=\"18\">&nbsp;</td>\n" +
            "    <td align=\"left\" valign=\"top\"> \n" +
            "      <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"helpert\">\n" +
            "        <tr> \n" +
            "          <td class=\"helpert_left\"></td>\n" +
            "          <td class=\"helpert_middle\">推荐版面</td>\n" +
            "          <td class=\"helpert_right\"></td>\n" +
            "          <td>&nbsp;</td>\n" +
            "        </tr>\n" +
            "      </table>\n" +
            "      <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"helper\">\n" +
            "              <tr> \n" +
            "                <td width=\"100%\" class=\"MainContentText\">\n" +
            "<ul style=\"margin: 5px 0px 0px 15px; padding: 0px;\">\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=NetResources\">网络资源</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Fansub\">字幕公社</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Oceania\">南十字星下</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Canada\">枫之国</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Weiqi\">纹枰论道</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Cyclone\">自行车运动</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Career_Plaza\">求职广场</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Bond\">债券和固定收益理财</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Flyers\">航空旅行</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=TrafficInfo\">交通信息</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=BeautyExchange\">交换美丽</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=HaiTao\">海淘</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Shopping\">购物</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Marvel\">聊斋鬼话</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=FairCity\">锦绣都·言情</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Wisdom\">儒释道</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Buddha\">居士林</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Bible\">圣经</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=GuoJiXue\">国计学</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=METech\">微电子技术</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Mentality\">心理</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Science\">科学</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=PreUnivEdu\">中小学数理化</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Paper\">论文</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Notebook\">笔记本电脑</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=LinuxApp\">Linux系统与应用</a></li>\n" +
            "</ul></td></tr>\n" +
            "      </table>\n" +
            "      <br>\n" +
            "      <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"helpert\">\n" +
            "        <tr> \n" +
            "          <td class=\"helpert_left\"></td>\n" +
            "          <td class=\"helpert_middle\">新开版面</td>\n" +
            "          <td class=\"helpert_right\"></td>\n" +
            "          <td>&nbsp;</td>\n" +
            "        </tr>\n" +
            "      </table>\n" +
            "      <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"helper\">\n" +
            "              <tr> \n" +
            "                <td width=\"100%\" class=\"MainContentText\">\n" +
            "<ul style=\"margin: 5px 0px 0px 15px; padding: 0px;\">\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Coffee\">咖啡时光</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=CRonaldo\">C·罗纳尔多</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=PeterSheng\">盛一伦</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Rainbow\">彩虹</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Messi\">梅西</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Jifenluohu\">积分落户</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=SNH48\">塞纳河48</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Gongyi\">爱心公益</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=AutoService\">汽车后服</a></li>\n" +
            "<li class=\"default\"><a href=\"bbsdoc.php?board=Dubbing\">配音</a></li>\n" +
            "</ul>\n" +
            "<p align=\"right\"><a href=\"bbsxmlbrd.php?flag=2\">&gt;&gt;更多</a></p>\n" +
            "\t</td></tr>\n" +
            "      </table><br/>\n" +
            "      <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"helpert\">\n" +
            "        <tr> \n" +
            "          <td class=\"helpert_left\"></td>\n" +
            "          <td class=\"helpert_middle\">人气排名</td>\n" +
            "          <td class=\"helpert_right\"></td>\n" +
            "          <td>&nbsp;</td>\n" +
            "        </tr>\n" +
            "      </table>\n" +
            "      <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"helper\">\n" +
            "              <tr> \n" +
            "                <td height=\"20\" class=\"MainContentText\">1. <a href=\"bbsdoc.php?board=FamilyLife\">家庭生活</a></td>\n" +
            "              </tr>\n" +
            "              <tr> \n" +
            "                <td height=\"20\" class=\"MainContentText\">2. <a href=\"bbsdoc.php?board=RealEstate\">房地产论坛</a></td>\n" +
            "              </tr>\n" +
            "              <tr> \n" +
            "                <td height=\"20\" class=\"MainContentText\">3. <a href=\"bbsdoc.php?board=CouponsLife\">辣妈羊毛党</a></td>\n" +
            "              </tr>\n" +
            "              <tr> \n" +
            "                <td height=\"20\" class=\"MainContentText\">4. <a href=\"bbsdoc.php?board=Picture\">贴图</a></td>\n" +
            "              </tr>\n" +
            "              <tr> \n" +
            "                <td height=\"20\" class=\"MainContentText\">5. <a href=\"bbsdoc.php?board=AutoWorld\">汽车世界</a></td>\n" +
            "              </tr>\n" +
            "              <tr> \n" +
            "                <td height=\"20\" class=\"MainContentText\">6. <a href=\"bbsdoc.php?board=OurEstate\">二手房交流</a></td>\n" +
            "              </tr>\n" +
            "              <tr> \n" +
            "                <td height=\"20\" class=\"MainContentText\">7. <a href=\"bbsdoc.php?board=NewExpress\">水木特快</a></td>\n" +
            "              </tr>\n" +
            "              <tr> \n" +
            "                <td height=\"20\" class=\"MainContentText\">8. <a href=\"bbsdoc.php?board=EconForum\">经济论坛</a></td>\n" +
            "              </tr>\n" +
            "              <tr> \n" +
            "                <td height=\"20\" class=\"MainContentText\">9. <a href=\"bbsdoc.php?board=Occupier\">业主之家</a></td>\n" +
            "              </tr>\n" +
            "              <tr> \n" +
            "                <td height=\"20\" class=\"MainContentText\">10. <a href=\"bbsdoc.php?board=WorkLife\">职业生涯</a></td>\n" +
            "              </tr>\n" +
            "      </table>\n" +
            "\t  <br>\n" +
            "<a name=\"todaybless\"></a>\n" +
            "      <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"helpert\">\n" +
            "        <tr> \n" +
            "          <td class=\"helpert_left\"></td>\n" +
            "          <td class=\"helpert_middle\">今日祝福</td>\n" +
            "          <td class=\"helpert_right\"></td>\n" +
            "          <td>&nbsp;</td>\n" +
            "        </tr>\n" +
            "      </table>\n" +
            "      <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"helper\">\n" +
            "<tr> \n" +
            "<td class=\"MainContentText\">\n" +
            "<ul style=\"margin: 5px 0px 0px 15px; padding: 0px;\">\n" +
            "</ul></td></tr>\n" +
            "      </table>\n" +
            "\t  </td>\n" +
            "    <td width=\"10\">&nbsp;</td>\n" +
            "  </tr>\n" +
            "</table>\n" +
            "<br/>\n" +
            "<hr class=\"smth\">\n" +
            "<center>\n" +
            "<style type=\"text/css\">\n" +
            "<!--\n" +
            ".newsmth_bottom {\n" +
            "\tfont-size: 12px;\n" +
            "\ttext-decoration: none;\n" +
            "}\n" +
            "-->\n" +
            "</style>\n" +
            "<table border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\">\n" +
            "  <tr>\n" +
            "    <td align=\"center\"><table width=\"100%\"  border=\"0\" cellspacing=\"5\" cellpadding=\"0\">\n" +
            "      <tr align=\"center\">\n" +
            "        <td class=\"newsmth_bottom\"><a href=\"/about/index.html\" target=\"_blank\">关于NewSMTH</a></td>\n" +
            "\t\t<td  class=\"newsmth_bottom\">|</td>\n" +
            "        <td class=\"newsmth_bottom\"><a href=\"/about/ad.html\" target=\"_blank\">广告服务</a></td>\n" +
            "\t\t<td  class=\"newsmth_bottom\">|</td>\n" +
            "        <td class=\"newsmth_bottom\"><a href=\"/about/contact.html\" target=\"_blank\">联系方式</a></td>\n" +
            "\t\t<td  class=\"newsmth_bottom\">|</td>\n" +
            "        <td class=\"newsmth_bottom\"><a href=\"/about/announce.html\" target=\"_blank\">法律声明</a></td>\n" +
            "\t\t<td  class=\"newsmth_bottom\">|</td>\n" +
            "        <td class=\"newsmth_bottom\"><a href=\"/about/privacy.html\" target=\"_blank\">隐私权保护</a></td>\n" +
            "\t\t<td  class=\"newsmth_bottom\">|</td>\n" +
            "<!--        <td class=\"newsmth_bottom\"><a href=\"http://www.mtech.cn\" target=\"_blank\">明睿博科技</a></td>\n" +
            "\t\t<td  class=\"newsmth_bottom\">|</td>-->\n" +
            "        <td class=\"newsmth_bottom\"><a href=\"http://www.miibeian.gov.cn\" target=\"_blank\">京ICP证110768号</a></td>\n" +
            "        <td  class=\"newsmth_bottom\">|</td>\n" +
            "        <td class=\"newsmth_bottom\"><a href=\"/sitemap.html\" target=\"_blank\">网站地图</a></td>\n" +
            "      </tr>\n" +
            "    </table></td>\n" +
            "  </tr>\n" +
            "  <tr>\n" +
            "    <td align=\"center\"><table width=\"100%\"  border=\"0\" cellspacing=\"5\" cellpadding=\"0\">\n" +
            "      <tr>\n" +
            "        <td align=\"right\" class=\"newsmth_bottom\"><span style=\"font-family:'Times New Roman', Times, serif;;\">&copy;</span>2004-2014 水木联创 版权所有</td>\n" +
            "        <td width=\"10\">&nbsp;</td>\n" +
            "\t\t<td class=\"newsmth_bottom\">Mail to: <a href=\"mailto:newsmthcom@139.com\">newsmthcom@139.com</a></td>\n" +
            "      </tr>\n" +
            "    </table></td>\n" +
            "  </tr>\n" +
            " <tr>\n" +
            "    <td align=\"center\"><a href=\"http://www.hd315.gov.cn/beian/view.asp?bianhao=010202005082200559\" target=\"_blank\"><img src=\"/images/hd315.gif\" border=\"0\" alt=\"水木社区\" /></a></td>\n" +
            "  </tr>\n" +
            "</table>\n" +
            "</center>\n" +
            "<script type=\"text/javascript\">\n" +
            "var _bdhmProtocol = ((\"https:\" == document.location.protocol) ? \" https://\" : \" http://\");\n" +
            "document.write(unescape(\"%3Cscript src='\" + _bdhmProtocol + \"hm.baidu.com/h.js%3F9c7f4d9b7c00cb5aba2c637c64a41567' type='text/javascript'%3E%3C/script%3E\"));\n" +
            "</script>\n" +
            "<br>\n" +
            "</body>\n" +
            "</html>\n";

    public void testParseHotTopics() throws Exception {
        List<Topic> results = SMTHHelper.ParseHotTopics(sGuidanceResponse);

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
}