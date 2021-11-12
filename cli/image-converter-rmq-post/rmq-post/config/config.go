// harvestor is an application for recursively traverse directories and find media files.
// send each file of our interest to to object-store-api and map it back to the metadata
// For more information please refer to https://github.com/AAFC-BICoE/object-store-harvestor/blob/dev/doc/design.md

// Package config provides a functionality to read from yml config file
// and provides values for each key in the file
// the package is splitted into multiple for simplicity and readability
package config

import (
	"fmt"
	"github.com/spf13/viper"
	"log"
	"os"
	"path/filepath"
	"strings"
)

// Config wrapper struct
type Configuration struct {
	Logger   LoggerConfiguration   // Logger config
	App      AppConfiguration      // App config
	RabbitMQ RabbitMQConfiguration // RabbitMQ config
}

// validation helpers
var ValidConfigFileExtension = "yml"

// define global empty configuration
var conf Configuration

// Getting Configuration struct
func GetConf() *Configuration {
	return &conf
}

// Loading from yml config file into our struct
func Load(filename string) {
	err := readFromFile(filename)
	if err != nil {
		log.Fatal(err)
	}
}

// Reading from file
func readFromFile(filename string) error {
	// get path and file
	path, file := filepath.Split(filename)

	// validation on supported extention
	if !isValidConfigFile(file) {
		err := fmt.Errorf("config file:(%q) is not supported with extention:(%q)", file, getFileExtension(file))
		return err
	}

	// define file name
	name := getFileName(file)
	// define file extension
	extension := getFileExtension(file)
	// init new viper
	v := viper.New()
	// passing file to viper
	// config file name without extension
	v.SetConfigName(name)
	// config file extension
	v.SetConfigType(extension)
	// config itself file path
	v.AddConfigPath("." + string(os.PathSeparator))
	// config file path
	v.AddConfigPath(path)
	// in case we have .env
	// read value ENV variable
	v.AutomaticEnv()

	// Reading from yml file
	err := v.ReadInConfig()
	if err != nil {
		return err
	}

	// Unmarshal to predefined struct
	err = v.Unmarshal(&conf)
	if err != nil {
		return err
	}

	// returning Configuration
	return nil
}

// check if the config type supported
func isValidConfigFile(file string) bool {
	return getFileExtension(file) == ValidConfigFileExtension
}

// get file extension
func getFileExtension(filename string) string {
	return strings.ToLower(strings.TrimPrefix(filepath.Ext(filename), "."))
}

// get file name without extension
func getFileName(file string) string {
	return strings.TrimSuffix(file, filepath.Ext(file))
}
