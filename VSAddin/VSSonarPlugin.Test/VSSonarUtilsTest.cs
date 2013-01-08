using System;
using System.IO;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace VSSonarPlugin.Test
{
    [TestClass]
    public class VSSonarUtilsTest
    {
        private string fileName = System.IO.Path.GetTempPath() + "\\.vssonar";
        [TestInitialize]
        public void SetUp()
        {
            if(File.Exists(fileName))
            {
                File.Delete(fileName);
            }
        }

        [TestCleanup]
        public void TearDown()
        {
            if (File.Exists(fileName))
            {
                File.Delete(fileName);
            }
        }


        [TestMethod]
        public void TestFileDoesNotExist()
        {
            VSSonarUtils.WriteDataToConfigurationFile("key", "data", fileName);
            string[] lines = System.IO.File.ReadAllLines(fileName);
            Assert.AreEqual(lines.Length, 1);
            Assert.AreEqual(lines[0], "key=data");
        }

        [TestMethod]
        public void TestFileExistAddNewData()
        {
            VSSonarUtils.WriteDataToConfigurationFile("key", "data", fileName);
            string[] lines = System.IO.File.ReadAllLines(fileName);
            Assert.AreEqual(lines.Length, 1);
            Assert.AreEqual(lines[0], "key=data");
            VSSonarUtils.WriteDataToConfigurationFile("key1", "data1", fileName);
            lines = System.IO.File.ReadAllLines(fileName);
            Assert.AreEqual(lines.Length, 2);
            Assert.AreEqual(lines[0], "key=data");
            Assert.AreEqual(lines[1], "key1=data1");
        }

        [TestMethod]
        public void TestFileExistReplace()
        {
            VSSonarUtils.WriteDataToConfigurationFile("key", "data", fileName);
            string[] lines = System.IO.File.ReadAllLines(fileName);
            Assert.AreEqual(lines.Length, 1);
            Assert.AreEqual(lines[0], "key=data");
            VSSonarUtils.WriteDataToConfigurationFile("key", "data2", fileName);
            lines = System.IO.File.ReadAllLines(fileName);
            Assert.AreEqual(lines.Length, 1);
            Assert.AreEqual(lines[0], "key=data2");
        }

        [TestMethod]
        public void TestMultiplesData()
        {
            VSSonarUtils.WriteDataToConfigurationFile("key", "data", fileName);
            VSSonarUtils.WriteDataToConfigurationFile("key1", "data1", fileName);
            VSSonarUtils.WriteDataToConfigurationFile("key2", "data2", fileName);
            string[] lines = System.IO.File.ReadAllLines(fileName);
            Assert.AreEqual(lines.Length, 3);
            Assert.AreEqual(lines[0], "key=data");
            Assert.AreEqual(lines[1], "key1=data1");
            Assert.AreEqual(lines[2], "key2=data2");
        }

        [TestMethod]
        public void TestReadData()
        {
            VSSonarUtils.WriteDataToConfigurationFile("key", "data", fileName);
            VSSonarUtils.WriteDataToConfigurationFile("key1", "data1", fileName);
            VSSonarUtils.WriteDataToConfigurationFile("key2", "data2", fileName);
            Assert.AreEqual(VSSonarUtils.ReadPropertyFromFile("key", fileName), "data");
            Assert.AreEqual(VSSonarUtils.ReadPropertyFromFile("key1", fileName), "data1");
            Assert.AreEqual(VSSonarUtils.ReadPropertyFromFile("key2", fileName), "data2");
        }

    }
}
