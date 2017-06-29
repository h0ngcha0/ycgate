package com.yc17.ycgate.prs.en

import com.yc17.ycgate.GateConfig._
import com.yc17.ycgate.sesame._
import java.net.{URI, URL}
import java.nio.file.Paths
import org.scalatest.{FunSpec, Matchers}

class FullAddressJapeTest extends FunSpec
    with Matchers
    with SesameControllerTestDSL
{
  override def gateHomeURL = gateHome.toUri.toURL
  override def prs = List(
    SesameTransducer(
      Paths.get(japeHome.toString, "en", "FullAddress.jape").toUri.toString,
      "FullAddress rule"
    )
  )

  // NOTE: This jape rule doesn't rely on the address type coming from
  //       ANNIE but operate at a lower level. so hopefully this can
  //       match addresses better

  // NOTE: some explanation to some of the macros
  //       FULL_ADDRESS_PREFIX:
  //       e.g. `with principal registered place of business cc mailing address is located at`
  //       SHORT_ADDRESS_PREFIX:
  //       e.g. `at`
  //       ADDRESS_PREFIX:
  //       either full or short address prefix
  describe("Run fullAddress.jape") {
    it("should run ukAddress1 rule") {
      val refStr = "37 Falmouth Road, Bristol, Avon, BS7 8PX, United Kingdom"

      val outputAnnot = (str: String) => {
        str ^ Feature("rule", "fullAddress1") ^ Feature("Type", "Text")
      }

      val firstLine = "37 Falmouth Road" <<= "Street" >>=
                      outputAnnot("FirstLine")
      val city      = "Bristol" <<= "Lookup" ^ Feature("minorType", "city") >>=
                      outputAnnot("City")
      val region    = "Avon" <<= "Lookup" ^ Feature("minorType", "region") >>=
                      outputAnnot("Region")
      val postCode  = "BS7 8PX" <<= "Postcode" >>= "Postcode"
      val country   = "United Kingdom" <<= "Lookup" ^ Feature("minorType", "country") >>=
                      outputAnnot("Country")

      val address   = firstLine + ", " + city + ", " + region + ", " +
                      postCode + ", " + country >>= outputAnnot("FullAddress")

      assert(refStr == address.str)
      address.verify()
    }

    it("should run ukAddress2 rule") {
      // deals with the situation where everything else is ok, like in ukAddress1, except for `city`
      val refStr = "37 Falmouth Road, Hartley Wintney, Avon, BS7 8PX, United Kingdom"

      val outputAnnot = (str: String) => str ^ Feature("Type", "Text")

      val firstLine = "37 Falmouth Road" <<= "Street" >>= outputAnnot("FirstLine")
      val hartley   = "Hartley".upperInitToken
      val wintney   = "Wintney".upperInitToken
      val city      = hartley + " " + wintney >>= outputAnnot("City")
      val region    = "Avon" <<= "Lookup" ^ Feature("minorType", "region") >>= outputAnnot("Region")
      val postCode  = "BS7 8PX" <<= "Postcode" >>= "Postcode"
      val country   = "United Kingdom" <<= "Lookup" ^ Feature("minorType", "country") >>= outputAnnot("Country")

      val address   = firstLine + ", " + city + ", " + region + ", " +
                      postCode + ", " + country >>=
                      "FullAddress" ^ Feature("rule", "fullAddress2") ^ Feature("Type", "Text")

      assert(refStr == address.str)
      address.verify()
    }

    it("should run usAddress rule") {
      // deals with the situation where everything else is ok, like in ukAddress1, except for `city`
      val refStr = "5480 Great America Parkway, Santa Clara, CA 95054, United States"

      val outputAnnot = (str: String) => str ^ Feature("Type", "Text")

      val roadNumber= "5480".numberToken
      val street    = "Great".upperInitToken |+| "America".upperInitToken |+| "Parkway".upperInitToken
      val firstLine = roadNumber |+| street
      val city      = "Santa Clara" <<= "Lookup" ^ Feature("minorType", "city")
      val region    = "CA" <<= "Lookup" ^ Feature("minorType", "USstates") >>= outputAnnot("Region")
      val postCode  = "95054".numberToken >>= "Postcode"
      val country   = "United States" <<= "Lookup" ^ Feature("minorType", "country") >>= outputAnnot("Country")

      val address   = firstLine + ", " + city + ", " + region + " " + postCode +
                      ", " + country >>=
                      "FullAddress" ^ Feature("rule", "usAddress") ^ Feature("Type", "Text")

      assert(refStr == address.str)
      address.verify()
    }

    ignore("should run genericAddressStartingWithAT rule") {
      // NOTE: I think this rule can not identify the following string as address.
      //       because the country was in the middle at the LHS of the rule. need to revisit.
      val refStr = "registered office at 6th Floor, South Bank House, Barrow Street, Dublin 4 Ireland"
    }

    it("should run adressWithRegionAndCountryStartingWithAT rule") {
      val refStr = "at The Courtyard, 2-4 London Road, Newbury, Berkshire RG13 1JL, England"
      val rule = "atAddress2"

      val prefix     = "at".token >>=
                       "AddressPrefix" ^ Feature("rule", rule) ^ Feature("Type", "Text")
      val street     = "2-4 London Road" <<= "Street"
      val firstLine  = "The".token + " " + "Courtyard".token + ", " + street
      val city       = "Newbury".upperInitToken
      val region     = "Berkshire" <<= "Lookup" ^ Feature("minorType", "region")
      val postcode   = "RG13".token + " " + "1JL".token
      val country    = "England" <<= "Lookup" ^ Feature("minorType", "country")

      val address    = firstLine + ", " + city + ", " + region + " " +
                       postcode + ", " + country >>=
                       "FullAddress" ^ Feature("rule", rule) ^ Feature("Type", "Text")

      val addrPrefix = prefix + " " + address
      assert(refStr == addrPrefix.str)
      addrPrefix.verify()
    }

    // FIXME
    ignore("should run prefixedAdressWithPostCodeAndCity rule") {
      val refStr = "at Hubwoo House, Mere Park, Dedmere Road, Marlow UK SL7 1PD"

      val outputAnnot = (str: String) => str ^ Feature("Type", "Text")

      val rule = "prefixedAdressWithPostCodeAndCity"

      val prefix    = "at".token >>=
                      "AddressPrefix" ^ Feature("rule", rule) ^ Feature("Type", "Text")
      val street    = "Dedmere Road" <<= "Street"
      val firstLine = "Hubwoo".token + " " + "House".token + ", " +
                      "Mere".token + " " + "Park".token + ", " + street >>= "FirstLine"
      val city      = "Marlow".upperInitToken >>= outputAnnot("City")
      val country   = "UK" <<= "Lookup" ^ Feature("minorType", "country") >>= outputAnnot("Country")
      val postcode  = "SL7 1PD" <<= "Postcode" >>= outputAnnot("Postcode")

      val address   = firstLine + ", " + city + " " + country + " " + postcode >>=
                      "FullAddress" ^ Feature("rule", rule) ^ Feature("Type", "Text")

      val addrPrefix = prefix + " " + address
      assert(refStr == addrPrefix.str)
      addrPrefix.verify()
    }

    it("should run prefixedAdressWithPostCodeAndCity2 rule") {
      val refStr = "registered office at Hubwoo House, Dedmere Road, Marlow UK SL7 1PD"

      val outputAnnot = (str: String) => str ^ Feature("Type", "Text")

      val rule = "prefixedAdressWithPostCodeAndCity2"

      val prefix    = "registered".token + " " + "office".token + " " + "at".token
                      "AddressPrefix" ^ Feature("rule", rule) ^ Feature("Type", "Text")
      val street    = "Dedmere Road" <<= "Street"
      val firstLine = "Hubwoo".token + " " + "House".token + ", " + street >>= "FirstLine"
      val city      = "Marlow".upperInitToken >>= outputAnnot("City")
      val country   = "UK" <<= "Lookup" ^ Feature("minorType", "country") >>= outputAnnot("Country")
      val postcode  = "SL7 1PD" <<= "Token" ^ Feature("kind", "number") ^ Feature("length", 7) >>= outputAnnot("Postcode")

      val address   = firstLine + ", " + city + " " + country + " " + postcode >>=
                      "FullAddress" ^ Feature("rule", rule) ^ Feature("Type", "Text")

      val addrPrefix = prefix + " " + address
      assert(refStr == addrPrefix.str)
      addrPrefix.verify()
    }

    it("should run prefixedAdressWithPostCodeAndCityEUROPE rule") {
      val refStr = "registered office at Hubwoo House, Dedmere Road, SL7 1PD Marlow UK"
      val outputAnnot = (str: String) => str ^ Feature("Type", "Text")

      val rule = "prefixedAdressWithPostCodeAndCityEUROPE"

      val prefix    = "registered".token + " " + "office".token + " " + "at".token
                      "AddressPrefix" ^ Feature("rule", rule) ^ Feature("Type", "Text")
      val street    = "Dedmere Road" <<= "Street"
      val firstLine = "Hubwoo".token + " " + "House".token + ", " + street >>= "FirstLine"
      val city      = "Marlow".upperInitToken >>= outputAnnot("City")
      val country   = "UK" <<= "Lookup" ^ Feature("minorType", "country") >>= outputAnnot("Country")
      val postcode  = "SL7 1PD" <<= "Token" ^ Feature("kind", "number") ^ Feature("length", 7) >>= outputAnnot("Postcode")

      val address   = firstLine + ", " + postcode + " " + city + " " + country >>=
                      "FullAddress" ^ Feature("rule", rule) ^ Feature("Type", "Text")

      val addrPrefix = prefix + " " + address
      assert(refStr == addrPrefix.str)
      addrPrefix.verify()
    }

    it("should run StreetRegionCountry rule") {
      val refStr = "PO Box 1746 Saxonwold 2132 Gauteng South Africa"

      val outputAnnot = (str: String) => str ^ Feature("Type", "Text")

      val street   = "PO Box 1746" <<= "Street"
      val city     = "Saxonwold".upperInitToken >>= outputAnnot("City")
      val postcode = "2132".numberToken >>= outputAnnot("Postcode")
      val region   = "Gauteng" <<= "Lookup" ^ Feature("minorType", "region") >>= outputAnnot("Region")
      val country  = "South Africa" <<= "Lookup" ^ Feature("minorType", "country") >>= outputAnnot("Country")

      val address  = street + " " + city + " " + postcode + " " + region + " " + country >>=
                     "FullAddress" ^ Feature("rule", "StreetRegionCountry") ^ Feature("Type", "Text")
      assert(refStr == address.str)
      address.verify()
    }

    // FIXME
    ignore("should run StreetCityRegion rule") {
      // allow something that looks like a `city` and allow some noice between `city` and `region`
      val refStr = "100 Zhongshan Road, nan jing, greater Jiangsu Province 12345, P.R.China"

      val outputAnnot = (str: String) => str ^ Feature("Type", "Text")

      val street     = "Zhongshan Road" <<= "Street"
      val firstLine  = "100".numberToken + " " + street >>= outputAnnot("FirstLine")
      val city       = "nan".upperInitToken + " " + "jing".upperInitToken >>= outputAnnot("City")
      val noise      = "greater".token
      val region     = "Jiangsu Province" <<= "Lookup" ^ Feature("minorType", "region") >>= outputAnnot("Region")
      val postcode   = "12345".numberToken >>= outputAnnot("Postcode")
      val country    = "P.R.China" <<= "Lookup" ^ Feature("minorType", "country") >>= outputAnnot("Country")

      val address    = firstLine + ", " + city + ", " + noise + " " +
                       region + " " + postcode + ", " + country >>=
                      "FullAddress" ^ Feature("rule", "StreetCityRegion") ^ Feature("Type", "Text")

      assert(refStr == address.str)
      address.verify()
    }

    it("should run StreetCityCountry rule") {
      // variant where we have a country but not necessarily a region or a postcode
      val refStr = "Main Street 1234, Anytown, USA"

      val outputAnnot = (str: String) => str ^ Feature("Type", "Text")

      val street     = "Main Street" <<= "Street"
      val firstLine  = street + " " + "1234".numberToken >>= outputAnnot("FirstLine")
      val city       = "Anytown".upperInitToken >>= outputAnnot("City")
      val country    = "USA" <<= "Lookup" ^ Feature("minorType", "country") >>= outputAnnot("Country")

      val address    = firstLine + ", " + city + ", " + country >>=
                      "FullAddress" ^ Feature("rule", "StreetCityCountry") ^ Feature("Type", "Text")

      assert(refStr == address.str)
      address.verify()
    }

    it("should run JustCityRegion rule") {
      val refStr = "Nanjing, Jiangsu"

      val outputAnnot = (str: String) => str ^ Feature("Type", "Text")

      val city    = "Nanjing" <<= "Lookup" ^ Feature("minorType", "city") >>= outputAnnot("City")
      val region  = "Jiangsu" <<= "Lookup" ^ Feature("minorType", "province") >>= outputAnnot("Region")

      val address = city + ", " + region >>=
                    "FullAddress" ^ Feature("rule", "CityRegion") ^ Feature("Type", "Text")

      assert(refStr == address.str)
    }

    it("should run JustUSCityRegion rule") {
      val refStr = "Nanjing, Jiangsu"

      val outputAnnot = (str: String) => str ^ Feature("Type", "Text")

      val city    = "Nanjing" <<= "Lookup" ^ Feature("minorType", "regionUSACities") >>= outputAnnot("City")
      val region  = "Jiangsu" <<= "Lookup" ^ Feature("minorType", "province") >>= outputAnnot("Region")

      val address = city + ", " + region >>=
                    "FullAddress" ^ Feature("rule", "USCityRegion") ^ Feature("Type", "Text")

      assert(refStr == address.str)
    }

    it("should run ORGSandPD rule") {
      val refStr = """LineSider Communications, Inc. 529 Main St., Charles town, !^LA 02129 (the "Company")"""

      val outputAnnot = (str: String) => str ^ Feature("Type", "Text")

      val org     = "LineSider Communications, Inc." <<= "Lookup" ^ Feature("minorType", "YcOrganization")
      val street  = "529 Main St." <<= "Street"
      val noise   = "Charles".token + " " + "town".token + ", " + "!^".token
      val region  = "LA" <<= "Lookup" ^ Feature("minorType", "province") >>= outputAnnot("Region")
      val postcode= "02129".numberToken
      val party   = """(the "Company")""" <<= "PartyDescriptorBlock"

      val address = street + ", " + noise + region + " " + postcode
                    "FullAddress" ^ Feature("rule", "ORGSandPD") ^ Feature("Type", "Text")

      val entireStr = org + " " + address + " " + party

      assert(refStr == entireStr.str)
      entireStr.verify()
    }
  }
}
