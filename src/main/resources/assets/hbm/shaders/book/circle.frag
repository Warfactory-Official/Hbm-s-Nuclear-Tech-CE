#version 120

uniform sampler2D mainTex;
uniform float angle;

varying vec2 texCoord;

void main(){
	vec2 loc = normalize(texCoord*2 - 1);
	gl_FragColor = texture2D(mainTex, texCoord) * int(dot(vec2(0, 1), loc) <= angle);
}
