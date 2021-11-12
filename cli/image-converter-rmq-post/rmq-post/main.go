// converter is an application for converting CR2 file to tiff on demand
package main

import (
	"converter/config"
	"converter/rabbitmqclient"
	"log"
	"os"
)

// main returns nothing
// this is the entry point for the app
func main() {
	// Getting our Configuration
	filename := getFileName()
	// Load yml config file
	config.Load(filename)
	rabbitmqclient.InitRabbitmqClient()
}

// very simple function to process arg
// could be doene with Package flag
// just to simple to use OS
// helper function to read args
func getFileName() string {
	// assign all args
	args := os.Args
	// check if any args are present
	// first arg is always an executable file
	if len(args) == 1 {
		example := "(example : /app/converter_config.yml)"
		err := "Application requires an argument as a string to a config file, none has been provided ||| " + example
		log.Fatal(err)
	}
	// assign the arg as a yml config file name
	filename := args[1]
	// return it
	return filename
}
