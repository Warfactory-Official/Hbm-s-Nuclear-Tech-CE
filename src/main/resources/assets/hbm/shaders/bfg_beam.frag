#version 120

uniform sampler2D mainTex;

varying vec2 texCoord;

void main(){
	vec4 tex = texture2D(mainTex, texCoord);
	gl_FragColor = tex * vec4(gl_Color.rgb*2, gl_Color.a);
}
