package com.example.toilet4all

data class Toilet(
    var tid: Int,                                   // 화장실ID
    var toiletType: String,                         // 구분
    var tolietNm: String,                           // 화장실명
    var rdnmadr: String,                            // 소재지도로명주소
    var lnmdar: String,                             // 소재지지번주소
    var unisexToiletYn: String,                     // 남녀공용화장실여부
    var menToiletBowlNumber: Int,                   // 남성용-대변기수
    var menUrineNumber: Int,                        // 남성용-소변기수
    var menHandicapToiletBowlNumber: Int,           // 남성용-장애인용대변기수
    var menHandicapUrinalNumber: Int,               // 남성용-장애인용대변기수
    var menChildrenToiletBottleNumber: Int,         // 남성용-장애인용대변기수
    var menChildrenUrinalNumber: Int,               // 남성용-장애인용대변기수
    var ladiesToiletBowlNumber: Int,                // 여성용-대변기수
    var ladiesHandicapToiletBowlNumber: Int,        // 여성용-장애인용대변기수
    var ladiesChildrenToiletBowlNumber: Int,        // 여성용-어린이용대변기수
    var institutionNm: String,                      // 관리기관명
    var phoneNumber: String,                        // 전화번호
    var openTime: String,                           // 개방시간
    var installationYear: String,                   // 설치년도
    var latitude: Double,                           // 위도
    var hardness: Double,                           // 경도
    var referenceData: String                       // 데이터기준일자
) {
}