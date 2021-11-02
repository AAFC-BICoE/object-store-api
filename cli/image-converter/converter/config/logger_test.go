package config

import (
	"github.com/stretchr/testify/assert"
	"regexp"
	"testing"
)

func TestGetLevel(t *testing.T) {
	var file = "../converter_config.yml"
	Load(file)
	conf := GetConf()
	assert.Regexp(t, regexp.MustCompile(`^[A-Za-z]{1,7}$`), conf.Logger.GetLevel())

}

func TestGetPath(t *testing.T) {
	var file = "../converter_config.yml"
	Load(file)
	conf := GetConf()
	assert.Equal(t, true, len(conf.Logger.GetPath()) > 0)
}

func TestGetFile(t *testing.T) {
	var file = "../converter_config.yml"
	Load(file)
	conf := GetConf()
	assert.Equal(t, "converter.log", conf.Logger.GetFile())
}
