// Package config provides a functionality to read from yml config file
// and provides values for each key in the file
// the package is splitted into multiple for simplicity and readability
// App config
package config

// App struct
type AppConfiguration struct {
	Release string // not used yet
	Name    string // not used yet
	Env     string // For now used to define cluster vs PC
}

// Define all interfaces for this struct
type IAppConfiguration interface {
	GetRelease() string
	GetName() string
	GetEnvironment() string
}

// Implementation
func (a AppConfiguration) GetRelease() string {
	return a.Release
}

func (a AppConfiguration) GetName() string {
	return a.Name
}

func (a AppConfiguration) GetEnvironment() string {
	return a.Env
}
