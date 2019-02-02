package com.wsn.lib.wsb.config

import com.wsn.lib.wsb.protocol.EsbAnalyzer
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

open class ConfigurationImporter : DefaultHandler() {

    @JvmField
    protected val DATA_TYPE = "DataType"
    protected val DATA_TYPE_VALUE = "value"

    protected var elementConsumed = false
    protected val builder = StringBuilder()
    protected var dataTypeValue:Byte = 0
    private var valueType = -1
    private var signed:Boolean = false
    private var coefficient:Double = 0.toDouble()

    fun leadIn(filePath: String) = leadIn(File(filePath))

    fun leadIn(file: File) = leadIn(InputSource(file.toURI().toASCIIString()))

    fun leadIn(stream: InputStream) = leadIn(InputSource(stream))

    fun leadIn(source: InputSource): Boolean {
        try {
            val factory = SAXParserFactory.newInstance()
            val parser = factory.newSAXParser()
            parser.parse(source, this)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @Throws(SAXException::class)
    override fun startElement(uri:String?, localName:String?, qName:String?, attributes: Attributes?) {
        builder.setLength(0)
    }

    @Throws(SAXException::class)
    override fun characters(ch:CharArray?, start:Int, length:Int) {
        builder.append(ch, start, length)
    }

    @Throws(SAXException::class)
    override fun endElement(uri:String?, localName:String?, qName:String?) {
        elementConsumed = true
        when (qName) {
            DATA_TYPE_VALUE -> dataTypeValue = Integer.parseInt(builder.toString(), 16).toByte()
            "type" -> valueType = Integer.parseInt(builder.toString())
            "signed" -> signed = java.lang.Boolean.parseBoolean(builder.toString())
            "coefficient" -> coefficient = java.lang.Double.parseDouble(builder.toString())
            DATA_TYPE -> {
                //根据mValueType为DataType配备不同的ValueBuilder
                if (valueType != -1) {
                    EsbAnalyzer.setValueBuilder(dataTypeValue, valueType, signed, coefficient)
                    valueType = -1
                    coefficient = 1.0
                }
            }
            else -> {
                elementConsumed = false
            }
        }
    }
}