//Import the required liberaries
package first
import org.apache.commons.math3.util.ArithmeticUtils
import org.apache.commons
import org.apache.poi.ss.usermodel.{DataFormatter, Row, WorkbookFactory}
import java.io.{File, FileNotFoundException, FileOutputStream,IOException}

import collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

object first extends App {
  /** *********************************************************
   * This is the main class to read an excel file and generate the
   * Result sheet using Apache POI API
   */

  //This function helps to check if values is valid decimal number or not
  def isAllDigits(x: String) = x.matches("^(((\\d{1,3})(,\\d{3})*)|(\\d+))(.\\d+)?$")

  try {
    val file = new File("E:\\Personal\\InternationalBaseline2019-Final.xls") //Excel file to read
    val workbook = WorkbookFactory.create(file)
    val no_of_sheets = workbook.getNumberOfSheets()
    var result_sheet = workbook.createSheet("Result") //Create new sheet named Result to capture result data
    var row_num = 0
    var cell_num = 0
    var start_writing = true

    //Iterate through all the sheets in that excel file except first file as in our case first file do not
    // contain data to analyze, hence i start from 1
    for (i <- 1 to no_of_sheets - 1) {
      row_num = 1
      var sheet = workbook.getSheetAt(i) //get the sheet name
      var usa_data: Map[String, String] = Map.empty[String, String] //to store USA data, year as key and crop/other thing as value
      var world_data: Map[String, String] = Map.empty[String, String] //to store World data, year as key and crop/other thing as value
      var usa_found = false
      var world_found = false

      //iterate through all the rows of that particular sheet to reach the level where we found USA and World data
      for (row <- sheet) {
        var comparison_key = Option(row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)) //get year for key
        var comparison_value = Option(row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)) //get value used in analysis

        if (comparison_key != None) {
          if (comparison_key.get.toString().contains("USA                        " + sheet.getSheetName())) //if USA data found
            usa_found = true
          if (comparison_key.get.toString().contains("WORLD                      " + sheet.getSheetName())) //if World data found
            world_found = true
        }
        //If USA found then store its data in usa_data Map as key value pair (Year -> Supplied value i.e. Column A -> Column B)
        if (usa_found) {
          var new_row = sheet.getRow(row.getRowNum())
          for (r <- 1 to 13) {
            new_row = sheet.getRow(row.getRowNum() + r + 2)
            usa_data += (Option(new_row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)).get.toString() -> Option(new_row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)).get.toString())
          }
          usa_found = false
        }
        //If USA found then store its data in world_data Map as key value pair (Year -> Supplied value i.e. Column A -> Column B)
        if (world_found) {
          var new_row = sheet.getRow(row.getRowNum())
          for (r <- 1 to 13) {
            new_row = sheet.getRow(row.getRowNum() + r + 2)
            world_data += (Option(new_row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)).get.toString() -> Option(new_row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)).get.toString())
          }
          world_found = false
        }
      } //end loop of rows of that particular sheet

      //Iterate through USA data and match Keys with World data
      //This is the block where logic calculation is happening and writing to the Result sheet
      for ((us_key, us_value) <- usa_data) {
        var w_value = world_data(us_key)
        if (isAllDigits(us_value) & isAllDigits(w_value)) {
          var numerator = us_value.toDouble
          var denominator = w_value.toDouble
          var result = (numerator / denominator) * 100 //main logic to get the us contribution %
          //when writing first time for first sheet
          if (start_writing) {
            var header_row = result_sheet.createRow(0)
            var row = result_sheet.createRow(row_num)
            header_row.createCell(0).setCellValue("year")
            header_row.createCell(1).setCellValue("world_" + sheet.getSheetName())
            header_row.createCell(2).setCellValue("usa_" + sheet.getSheetName() + "_contribution%")
            row.createCell(cell_num).setCellValue(us_key)
            row.createCell(cell_num + 1).setCellValue(denominator)
            row.createCell(cell_num + 2).setCellValue(result)

          }
          //when writing for other sheets
          else {
            var header_row = result_sheet.getRow(0)
            var row = result_sheet.getRow(row_num)
            header_row.createCell(cell_num + 1).setCellValue("world_" + sheet.getSheetName())
            header_row.createCell(cell_num + 2).setCellValue("usa_" + sheet.getSheetName() + "_contribution%")
            row.createCell(cell_num + 1).setCellValue(denominator)
            row.createCell(cell_num + 2).setCellValue(result)

          }
          row_num += 1
        }
      } //end loop of US & World data of that particular sheet

      if (start_writing)
        cell_num = cell_num + 3
      else
        cell_num = cell_num + 2
      start_writing = false
    }
    //Save the complete workbook along with Result sheet  with file named as Result.xls
    println("Save excel file")
    val output_file = new FileOutputStream("E:\\Personal\\Result.xls")
    workbook.write(output_file)
    output_file.close()
  }
  catch {
    case ex: FileNotFoundException => {
      println("Provided file is missing.")
    }

    case ex: IOException => {
      println("Some IO Exception occurred")
    }
    case ex : ArithmeticException =>
    {
      println("Some Arithmetic Exception occurred during logic calculation.")
    }
  }
  finally {
    println("Exiting finally...")
  }
}
