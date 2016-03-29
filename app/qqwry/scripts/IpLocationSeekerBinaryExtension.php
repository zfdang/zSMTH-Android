<?php
/**
 * 使用php扩展pecl::QQWry和纯真的二进制数据库文件进行IP地址所在地理位置查询类
 * 该php扩展的项目文档地址为：http://www.surfchen.org/peclqqwry
 * @author york
 */
class IpLocationSeekerBinaryExtension {
	private $qqwry;
	
	public function __construct($qqwry_filepath){
		$this->qqwry = new qqwry($qqwry_filepath);
	}

	public function seek($ip_address){
		list($country, $area) =  $this->qqwry->q($ip_address);
		$country = iconv("gb18030", "utf-8", $country);
		$area    = iconv("gb18030", "utf-8", $area);
		return array($country, $area);
	}
}
