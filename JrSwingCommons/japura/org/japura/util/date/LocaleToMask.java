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
		// Croatian (Croatia) - croata (Croácia) - 15.02.2001.
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("zh")) {
	  if (country.equals("")) {
		// Chinese - chinês - 2001-2-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("TW")) {
		// Chinese (Taiwan) - chinês (Taiwan) - 2001/2/15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("SG")) {
		// Chinese (Singapore) - chinês (Cingapura) - 15-??-01
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CN")) {
		// Chinese (China) - chinês (China) - 2001-2-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("HK")) {
		// Chinese (Hong Kong) - chinês (Hong Kong, Região Admin. Especial da
		// China) - 2001?2?15?
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("ro")) {
	  if (country.equals("")) {
		// Romanian - romeno - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("RO")) {
		// Romanian (Romania) - romeno (Romênia) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("ca")) {
	  if (country.equals("")) {
		// Catalan - catalão - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("ES")) {
		// Catalan (Spain) - catalão (Espanha) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("vi")) {
	  if (country.equals("")) {
		// Vietnamese - vietnamita - 15-02-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("VN")) {
		// Vietnamese (Vietnam) - vietnamita (Vietnã) - 15-02-2001
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
		// Norwegian (Norway) - norueguês (Noruega) - 15.feb.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Norwegian - norueguês - 15.feb.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("NO")) {
		// Norwegian (Norway,Nynorsk) - norueguês (Noruega,Nynorsk) -
		// 15.feb.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("hu")) {
	  if (country.equals("")) {
		// Hungarian - húngaro - 2001.02.15.
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("HU")) {
		// Hungarian (Hungary) - húngaro (Hungria) - 2001.02.15.
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("lv")) {
	  if (country.equals("")) {
		// Latvian - letão - 2001.15.2
		return DateMask.YYYYDDMM;
	  }
	  if (country.equals("LV")) {
		// Latvian (Latvia) - letão (Letônia) - 2001.15.2
		return DateMask.YYYYDDMM;
	  }
	} else if (lang.equals("hi")) {
	  if (country.equals("IN")) {
		// Hindi (India) - hindi (Índia) - ?? ??????, ????
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("lt")) {
	  if (country.equals("")) {
		// Lithuanian - lituano - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("LT")) {
		// Lithuanian (Lithuania) - lituano (Lituânia) - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("ga")) {
	  if (country.equals("IE")) {
		// Irish (Ireland) - irlandês (Irlanda) - 15 Feabh 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Irish - irlandês - 2001 Feabh 15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("th")) {
	  if (country.equals("")) {
		// Thai - tailandês - 15 ?.?. 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("TH")) {
		// Thai (Thailand) - tailandês (Tailândia) - 15 ?.?. 2544
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("TH")) {
		// Thai (Thailand,TH) - tailandês (Tailândia,TH) - ?? ?.?. ????
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("de")) {
	  if (country.equals("CH")) {
		// German (Switzerland) - alemão (Suíça) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("AT")) {
		// German (Austria) - alemão (Áustria) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("DE")) {
		// German (Germany) - alemão (Alemanha) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("LU")) {
		// German (Luxembourg) - alemão (Luxemburgo) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// German - alemão - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("fi")) {
	  if (country.equals("FI")) {
		// Finnish (Finland) - finlandês (Finlândia) - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Finnish - finlandês - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("fr")) {
	  if (country.equals("BE")) {
		// French (Belgium) - francês (Bélgica) - 15-févr.-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CA")) {
		// French (Canada) - francês (Canadá) - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("CH")) {
		// French (Switzerland) - francês (Suíça) - 15 févr. 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// French - francês - 15 févr. 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("LU")) {
		// French (Luxembourg) - francês (Luxemburgo) - 15 févr. 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("FR")) {
		// French (France) - francês (França) - 15 févr. 2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("sv")) {
	  if (country.equals("")) {
		// Swedish - sueco - 2001-feb-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("SE")) {
		// Swedish (Sweden) - sueco (Suécia) - 2001-feb-15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("bg")) {
	  if (country.equals("")) {
		// Bulgarian - búlgaro - 2001-2-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("BG")) {
		// Bulgarian (Bulgaria) - búlgaro (Bulgária) - 2001-2-15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("mk")) {
	  if (country.equals("")) {
		// Macedonian - macedônio - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("MK")) {
		// Macedonian (Macedonia) - macedônio (Macedônia) - 15.2.2001
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
		// Slovenian (Slovenia) - eslovênio (Eslovênia) - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Slovenian - eslovênio - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("sk")) {
	  if (country.equals("SK")) {
		// Slovak (Slovakia) - eslovaco (Eslováquia) - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Slovak - eslovaco - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("da")) {
	  if (country.equals("")) {
		// Danish - dinamarquês - 15-02-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("DK")) {
		// Danish (Denmark) - dinamarquês (Dinamarca) - 15-02-2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("uk")) {
	  if (country.equals("UA")) {
		// Ukrainian (Ukraine) - ucraniano (Ucrânia) - 15 ??? 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Ukrainian - ucraniano - 15 ??? 2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("is")) {
	  if (country.equals("")) {
		// Icelandic - islandês - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("IS")) {
		// Icelandic (Iceland) - islandês (Islândia) - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("it")) {
	  if (country.equals("")) {
		// Italian - italiano - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("IT")) {
		// Italian (Italy) - italiano (Itália) - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CH")) {
		// Italian (Switzerland) - italiano (Suíça) - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("mt")) {
	  if (country.equals("MT")) {
		// Maltese (Malta) - maltês (Malta) - 15 Fra 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Maltese - maltês - 15 Fra 2001
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
		// Serbian (Bosnia and Herzegovina) - sérvio (Bósnia-Herzegovina) -
		// 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("CS")) {
		// Serbian (Serbia and Montenegro) - sérvio (Sérvia e Montenegro) -
		// 15.02.2001.
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("RS")) {
		// Serbian (Serbia) - sérvio (Serbia) - 15.02.2001.
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Serbian - sérvio - 15.02.2001.
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("ME")) {
		// Serbian (Montenegro) - sérvio (Montenegro) - 15.02.2001.
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("ms")) {
	  if (country.equals("MY")) {
		// Malay (Malaysia) - malaio (Malásia) - 15 Februari 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Malay - malaio - 2001 Feb 15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("sq")) {
	  if (country.equals("AL")) {
		// Albanian (Albania) - albanês (Albânia) - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("")) {
		// Albanian - albanês - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("ko")) {
	  if (country.equals("")) {
		// Korean - coreano - 2001. 2. 15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("KR")) {
		// Korean (South Korea) - coreano (Coréia, Sul) - 2001. 2. 15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("ar")) {
	  if (country.equals("AE")) {
		// Arabic (United Arab Emirates) - árabe (Emirados Árabes Unidos) -
		// 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("IQ")) {
		// Arabic (Iraq) - árabe (Iraque) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("YE")) {
		// Arabic (Yemen) - árabe (Iêmen) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("QA")) {
		// Arabic (Qatar) - árabe (Catar) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("SA")) {
		// Arabic (Saudi Arabia) - árabe (Arábia Saudita) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("LB")) {
		// Arabic (Lebanon) - árabe (Líbano) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("KW")) {
		// Arabic (Kuwait) - árabe (Kuwait) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("SD")) {
		// Arabic (Sudan) - árabe (Sudão) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("SY")) {
		// Arabic (Syria) - árabe (Síria) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("BH")) {
		// Arabic (Bahrain) - árabe (Bahrain) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("TN")) {
		// Arabic (Tunisia) - árabe (Tunísia) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("JO")) {
		// Arabic (Jordan) - árabe (Jordânia) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("EG")) {
		// Arabic (Egypt) - árabe (Egito) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("MA")) {
		// Arabic (Morocco) - árabe (Marrocos) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("DZ")) {
		// Arabic (Algeria) - árabe (Argélia) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("LY")) {
		// Arabic (Libya) - árabe (Líbia) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Arabic - árabe - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("OM")) {
		// Arabic (Oman) - árabe (Omã) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("in")) {
	  if (country.equals("ID")) {
		// Indonesian (Indonesia) - indonésio (Indonésia) - 15 Feb 01
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Indonesian - indonésio - 2001 Feb 15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("cs")) {
	  if (country.equals("")) {
		// Czech - tcheco - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CZ")) {
		// Czech (Czech Republic) - tcheco (República Tcheca) - 15.2.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("el")) {
	  if (country.equals("CY")) {
		// Greek (Cyprus) - grego (Chipre) - 15 ??? 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("GR")) {
		// Greek (Greece) - grego (Grécia) - 15 ??? 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Greek - grego - 15 ??? 2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("pl")) {
	  if (country.equals("PL")) {
		// Polish (Poland) - polonês (Polônia) - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("")) {
		// Polish - polonês - 2001-02-15
		return DateMask.YYYYMMDD;
	  }
	} else if (lang.equals("pt")) {
	  if (country.equals("PT")) {
		// Portuguese (Portugal) - português (Portugal) - 15/Fev/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("BR")) {
		// Portuguese (Brazil) - português (Brasil) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("")) {
		// Portuguese - português - 15/Fev/2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("en")) {
	  if (country.equals("")) {
		// English - inglês - Feb 15, 2001
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("US")) {
		// English (United States) - inglês (Estados Unidos) - Feb 15, 2001
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("MT")) {
		// English (Malta) - inglês (Malta) - 15 Feb 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("GB")) {
		// English (United Kingdom) - inglês (Reino Unido) - 15-Feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("NZ")) {
		// English (New Zealand) - inglês (Nova Zelândia) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("PH")) {
		// English (Philippines) - inglês (Filipinas) - 02 15, 01
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("ZA")) {
		// English (South Africa) - inglês (África do Sul) - 15 Feb 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("IE")) {
		// English (Ireland) - inglês (Irlanda) - 15-Feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("IN")) {
		// English (India) - inglês (Índia) - 15 Feb, 2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("AU")) {
		// English (Australia) - inglês (Austrália) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("CA")) {
		// English (Canada) - inglês (Canadá) - 15-Feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("SG")) {
		// English (Singapore) - inglês (Cingapura) - Feb 15, 2001
		return DateMask.MMDDYYYY;
	  }
	} else if (lang.equals("ru")) {
	  if (country.equals("")) {
		// Russian - russo - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("RU")) {
		// Russian (Russia) - russo (Rússia) - 15.02.2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("et")) {
	  if (country.equals("EE")) {
		// Estonian (Estonia) - estoniano (Estônia) - 15.02.2001
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
		// Spanish (Panama) - espanhol (Panamá) - 02/15/2001
		return DateMask.MMDDYYYY;
	  }
	  if (country.equals("GT")) {
		// Spanish (Guatemala) - espanhol (Guatemala) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("NI")) {
		// Spanish (Nicaragua) - espanhol (Nicarágua) - 02-15-2001
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
		// Spanish (Mexico) - espanhol (México) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("UY")) {
		// Spanish (Uruguay) - espanhol (Uruguai) - 15/02/2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("DO")) {
		// Spanish (Dominican Republic) - espanhol (República Dominicana) -
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
		// Spanish (Colombia) - espanhol (Colômbia) - 15/02/2001
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
		// Spanish (Bolivia) - espanhol (Bolívia) - 15-02-2001
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
		// Dutch - holandês - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("NL")) {
		// Dutch (Netherlands) - holandês (Países Baixos) - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	  if (country.equals("BE")) {
		// Dutch (Belgium) - holandês (Bélgica) - 15-feb-2001
		return DateMask.DDMMYYYY;
	  }
	} else if (lang.equals("ja")) {
	  if (country.equals("JP")) {
		// Japanese (Japan) - japonês (Japão) - 2001/02/15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("JP")) {
		// Japanese (Japan,JP) - japonês (Japão,JP) - H13.02.15
		return DateMask.YYYYMMDD;
	  }
	  if (country.equals("")) {
		// Japanese - japonês - 2001/02/15
		return DateMask.YYYYMMDD;
	  }
	}
	return null;
  }

}
