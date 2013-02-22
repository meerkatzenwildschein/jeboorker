package org.japura.util.date;

import java.util.Locale;

/**
 * 
 * <P>
 * Copyright (C) 2011 Carlos Eduardo Leite de Andrade
 * <P>
 * This library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <P>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <P>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <A
 * HREF="www.gnu.org/licenses/">www.gnu.org/licenses/</A>
 * <P>
 * For more information, contact: <A HREF="www.japura.org">www.japura.org</A>
 * <P>
 * 
 * @author Carlos Eduardo Leite de Andrade
 */
final class LocaleToMask{

  public static DateMask getMask(Locale locale) {
	if (locale == null) {
	  locale = Locale.getDefault();
	}
	String lang = locale.getLanguage();
	String country = locale.getCountry();
	if (lang.equals("hr")) {
	  if (country.equals("")) {
		// Croatian - croata - 2001.02.15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("HR")) {
		// Croatian (Croatia) - croata (Cro�cia) - 15.02.2001.
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("zh")) {
	  if (country.equals("")) {
		// Chinese - chin�s - 2001-2-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("TW")) {
		// Chinese (Taiwan) - chin�s (Taiwan) - 2001/2/15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("SG")) {
		// Chinese (Singapore) - chin�s (Cingapura) - 15-??-01
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CN")) {
		// Chinese (China) - chin�s (China) - 2001-2-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("HK")) {
		// Chinese (Hong Kong) - chin�s (Hong Kong, Regi�o Admin. Especial da
		// China) - 2001?2?15?
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("ro")) {
	  if (country.equals("")) {
		// Romanian - romeno - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("RO")) {
		// Romanian (Romania) - romeno (Rom�nia) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("ca")) {
	  if (country.equals("")) {
		// Catalan - catal�o - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("ES")) {
		// Catalan (Spain) - catal�o (Espanha) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("vi")) {
	  if (country.equals("")) {
		// Vietnamese - vietnamita - 15-02-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("VN")) {
		// Vietnamese (Vietnam) - vietnamita (Vietn�) - 15-02-2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("tr")) {
	  if (country.equals("TR")) {
		// Turkish (Turkey) - turco (Turquia) - 15.?ub.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Turkish - turco - 15.?ub.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("no")) {
	  if (country.equals("NO")) {
		// Norwegian (Norway) - noruegu�s (Noruega) - 15.feb.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Norwegian - noruegu�s - 15.feb.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("NO")) {
		// Norwegian (Norway,Nynorsk) - noruegu�s (Noruega,Nynorsk) -
		// 15.feb.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("hu")) {
	  if (country.equals("")) {
		// Hungarian - h�ngaro - 2001.02.15.
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("HU")) {
		// Hungarian (Hungary) - h�ngaro (Hungria) - 2001.02.15.
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("lv")) {
	  if (country.equals("")) {
		// Latvian - let�o - 2001.15.2
		return DateMask.YYYYDDMM;
	  }
	  if (country.equals("LV")) {
		// Latvian (Latvia) - let�o (Let�nia) - 2001.15.2
		return DateMask.YYYYDDMM;
	  }
	} else if (lang.equals("hi")) {
	  if (country.equals("IN")) {
		// Hindi (India) - hindi (�ndia) - ?? ??????, ????
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("lt")) {
	  if (country.equals("")) {
		// Lithuanian - lituano - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("LT")) {
		// Lithuanian (Lithuania) - lituano (Litu�nia) - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("ga")) {
	  if (country.equals("IE")) {
		// Irish (Ireland) - irland�s (Irlanda) - 15 Feabh 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Irish - irland�s - 2001 Feabh 15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("th")) {
	  if (country.equals("")) {
		// Thai - tailand�s - 15 ?.?. 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("TH")) {
		// Thai (Thailand) - tailand�s (Tail�ndia) - 15 ?.?. 2544
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("TH")) {
		// Thai (Thailand,TH) - tailand�s (Tail�ndia,TH) - ?? ?.?. ????
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("de")) {
	  if (country.equals("CH")) {
		// German (Switzerland) - alem�o (Su��a) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("AT")) {
		// German (Austria) - alem�o (�ustria) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("DE")) {
		// German (Germany) - alem�o (Alemanha) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("LU")) {
		// German (Luxembourg) - alem�o (Luxemburgo) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// German - alem�o - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("fi")) {
	  if (country.equals("FI")) {
		// Finnish (Finland) - finland�s (Finl�ndia) - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Finnish - finland�s - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("fr")) {
	  if (country.equals("BE")) {
		// French (Belgium) - franc�s (B�lgica) - 15-f�vr.-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CA")) {
		// French (Canada) - franc�s (Canad�) - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("CH")) {
		// French (Switzerland) - franc�s (Su��a) - 15 f�vr. 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// French - franc�s - 15 f�vr. 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("LU")) {
		// French (Luxembourg) - franc�s (Luxemburgo) - 15 f�vr. 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("FR")) {
		// French (France) - franc�s (Fran�a) - 15 f�vr. 2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("sv")) {
	  if (country.equals("")) {
		// Swedish - sueco - 2001-feb-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("SE")) {
		// Swedish (Sweden) - sueco (Su�cia) - 2001-feb-15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("bg")) {
	  if (country.equals("")) {
		// Bulgarian - b�lgaro - 2001-2-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("BG")) {
		// Bulgarian (Bulgaria) - b�lgaro (Bulg�ria) - 2001-2-15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("mk")) {
	  if (country.equals("")) {
		// Macedonian - maced�nio - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("MK")) {
		// Macedonian (Macedonia) - maced�nio (Maced�nia) - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("be")) {
	  if (country.equals("")) {
		// Belarusian - bielo-russo - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("BY")) {
		// Belarusian (Belarus) - bielo-russo (Belarus) - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("sl")) {
	  if (country.equals("SI")) {
		// Slovenian (Slovenia) - eslov�nio (Eslov�nia) - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Slovenian - eslov�nio - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("sk")) {
	  if (country.equals("SK")) {
		// Slovak (Slovakia) - eslovaco (Eslov�quia) - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Slovak - eslovaco - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("da")) {
	  if (country.equals("")) {
		// Danish - dinamarqu�s - 15-02-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("DK")) {
		// Danish (Denmark) - dinamarqu�s (Dinamarca) - 15-02-2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("uk")) {
	  if (country.equals("UA")) {
		// Ukrainian (Ukraine) - ucraniano (Ucr�nia) - 15 ??? 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Ukrainian - ucraniano - 15 ??? 2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("is")) {
	  if (country.equals("")) {
		// Icelandic - island�s - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("IS")) {
		// Icelandic (Iceland) - island�s (Isl�ndia) - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("it")) {
	  if (country.equals("")) {
		// Italian - italiano - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("IT")) {
		// Italian (Italy) - italiano (It�lia) - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CH")) {
		// Italian (Switzerland) - italiano (Su��a) - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("mt")) {
	  if (country.equals("MT")) {
		// Maltese (Malta) - malt�s (Malta) - 15 Fra 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Maltese - malt�s - 15 Fra 2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("iw")) {
	  if (country.equals("")) {
		// Hebrew - hebraico - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("IL")) {
		// Hebrew (Israel) - hebraico (Israel) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("sr")) {
	  if (country.equals("BA")) {
		// Serbian (Bosnia and Herzegovina) - s�rvio (B�snia-Herzegovina) -
		// 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("CS")) {
		// Serbian (Serbia and Montenegro) - s�rvio (S�rvia e Montenegro) -
		// 15.02.2001.
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("RS")) {
		// Serbian (Serbia) - s�rvio (Serbia) - 15.02.2001.
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Serbian - s�rvio - 15.02.2001.
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("ME")) {
		// Serbian (Montenegro) - s�rvio (Montenegro) - 15.02.2001.
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("ms")) {
	  if (country.equals("MY")) {
		// Malay (Malaysia) - malaio (Mal�sia) - 15 Februari 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Malay - malaio - 2001 Feb 15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("sq")) {
	  if (country.equals("AL")) {
		// Albanian (Albania) - alban�s (Alb�nia) - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("")) {
		// Albanian - alban�s - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("ko")) {
	  if (country.equals("")) {
		// Korean - coreano - 2001. 2. 15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("KR")) {
		// Korean (South Korea) - coreano (Cor�ia, Sul) - 2001. 2. 15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("ar")) {
	  if (country.equals("AE")) {
		// Arabic (United Arab Emirates) - �rabe (Emirados �rabes Unidos) -
		// 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("IQ")) {
		// Arabic (Iraq) - �rabe (Iraque) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("YE")) {
		// Arabic (Yemen) - �rabe (I�men) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("QA")) {
		// Arabic (Qatar) - �rabe (Catar) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("SA")) {
		// Arabic (Saudi Arabia) - �rabe (Ar�bia Saudita) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("LB")) {
		// Arabic (Lebanon) - �rabe (L�bano) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("KW")) {
		// Arabic (Kuwait) - �rabe (Kuwait) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("SD")) {
		// Arabic (Sudan) - �rabe (Sud�o) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("SY")) {
		// Arabic (Syria) - �rabe (S�ria) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("BH")) {
		// Arabic (Bahrain) - �rabe (Bahrain) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("TN")) {
		// Arabic (Tunisia) - �rabe (Tun�sia) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("JO")) {
		// Arabic (Jordan) - �rabe (Jord�nia) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("EG")) {
		// Arabic (Egypt) - �rabe (Egito) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("MA")) {
		// Arabic (Morocco) - �rabe (Marrocos) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("DZ")) {
		// Arabic (Algeria) - �rabe (Arg�lia) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("LY")) {
		// Arabic (Libya) - �rabe (L�bia) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Arabic - �rabe - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("OM")) {
		// Arabic (Oman) - �rabe (Om�) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("in")) {
	  if (country.equals("ID")) {
		// Indonesian (Indonesia) - indon�sio (Indon�sia) - 15 Feb 01
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Indonesian - indon�sio - 2001 Feb 15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("cs")) {
	  if (country.equals("")) {
		// Czech - tcheco - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CZ")) {
		// Czech (Czech Republic) - tcheco (Rep�blica Tcheca) - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("el")) {
	  if (country.equals("CY")) {
		// Greek (Cyprus) - grego (Chipre) - 15 ??? 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("GR")) {
		// Greek (Greece) - grego (Gr�cia) - 15 ??? 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Greek - grego - 15 ??? 2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("pl")) {
	  if (country.equals("PL")) {
		// Polish (Poland) - polon�s (Pol�nia) - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("")) {
		// Polish - polon�s - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("pt")) {
	  if (country.equals("PT")) {
		// Portuguese (Portugal) - portugu�s (Portugal) - 15/Fev/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("BR")) {
		// Portuguese (Brazil) - portugu�s (Brasil) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Portuguese - portugu�s - 15/Fev/2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("en")) {
	  if (country.equals("")) {
		// English - ingl�s - Feb 15, 2001
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("US")) {
		// English (United States) - ingl�s (Estados Unidos) - Feb 15, 2001
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("MT")) {
		// English (Malta) - ingl�s (Malta) - 15 Feb 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("GB")) {
		// English (United Kingdom) - ingl�s (Reino Unido) - 15-Feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("NZ")) {
		// English (New Zealand) - ingl�s (Nova Zel�ndia) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("PH")) {
		// English (Philippines) - ingl�s (Filipinas) - 02 15, 01
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("ZA")) {
		// English (South Africa) - ingl�s (�frica do Sul) - 15 Feb 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("IE")) {
		// English (Ireland) - ingl�s (Irlanda) - 15-Feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("IN")) {
		// English (India) - ingl�s (�ndia) - 15 Feb, 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("AU")) {
		// English (Australia) - ingl�s (Austr�lia) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CA")) {
		// English (Canada) - ingl�s (Canad�) - 15-Feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("SG")) {
		// English (Singapore) - ingl�s (Cingapura) - Feb 15, 2001
		return DateMask.MMDDYYYY;
	  }
	} else if (lang.equals("ru")) {
	  if (country.equals("")) {
		// Russian - russo - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("RU")) {
		// Russian (Russia) - russo (R�ssia) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("et")) {
	  if (country.equals("EE")) {
		// Estonian (Estonia) - estoniano (Est�nia) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Estonian - estoniano - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("es")) {
	  if (country.equals("PE")) {
		// Spanish (Peru) - espanhol (Peru) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("PA")) {
		// Spanish (Panama) - espanhol (Panam�) - 02/15/2001
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("GT")) {
		// Spanish (Guatemala) - espanhol (Guatemala) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("NI")) {
		// Spanish (Nicaragua) - espanhol (Nicar�gua) - 02-15-2001
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("ES")) {
		// Spanish (Spain) - espanhol (Espanha) - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("US")) {
		// Spanish (United States) - espanhol (Estados Unidos) - feb 15, 2001
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("MX")) {
		// Spanish (Mexico) - espanhol (M�xico) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("UY")) {
		// Spanish (Uruguay) - espanhol (Uruguai) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("DO")) {
		// Spanish (Dominican Republic) - espanhol (Rep�blica Dominicana) -
		// 02/15/2001
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("VE")) {
		// Spanish (Venezuela) - espanhol (Venezuela) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Spanish - espanhol - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("EC")) {
		// Spanish (Ecuador) - espanhol (Equador) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CO")) {
		// Spanish (Colombia) - espanhol (Col�mbia) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CR")) {
		// Spanish (Costa Rica) - espanhol (Costa Rica) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CL")) {
		// Spanish (Chile) - espanhol (Chile) - 15-02-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("BO")) {
		// Spanish (Bolivia) - espanhol (Bol�via) - 15-02-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("SV")) {
		// Spanish (El Salvador) - espanhol (El Salvador) - 02-15-2001
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("PY")) {
		// Spanish (Paraguay) - espanhol (Paraguai) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("PR")) {
		// Spanish (Puerto Rico) - espanhol (Porto Rico) - 02-15-2001
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("AR")) {
		// Spanish (Argentina) - espanhol (Argentina) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("HN")) {
		// Spanish (Honduras) - espanhol (Honduras) - 02-15-2001
		return DateMask.MMDDYYYY;
	  }
	} else if (lang.equals("nl")) {
	  if (country.equals("")) {
		// Dutch - holand�s - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("NL")) {
		// Dutch (Netherlands) - holand�s (Pa�ses Baixos) - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("BE")) {
		// Dutch (Belgium) - holand�s (B�lgica) - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("ja")) {
	  if (country.equals("JP")) {
		// Japanese (Japan) - japon�s (Jap�o) - 2001/02/15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("JP")) {
		// Japanese (Japan,JP) - japon�s (Jap�o,JP) - H13.02.15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("")) {
		// Japanese - japon�s - 2001/02/15
		return DateMask.YYYYMMDD;
	  }
	}
	return null;
  }

}
