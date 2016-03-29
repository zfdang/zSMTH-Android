<?php

include "IpLocationSeekerBinary.php";

$sqlite_filepath = dirname(dirname(__FILE__)) . "/qqwry.db";
$qqwry_filepath = dirname(dirname(__FILE__)) . "/qqwry.dat";
$seeker = new IpLocationSeekerBinary($qqwry_filepath);
$seeker->saveAsSqlite($sqlite_filepath);
