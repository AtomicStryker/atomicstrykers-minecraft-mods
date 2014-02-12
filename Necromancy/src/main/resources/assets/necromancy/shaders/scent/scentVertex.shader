#version 120

void main()
{
	gl_FrontColor = vec4(0.6*noise1(5), 0.3, 0.4, 0.1);
	gl_BackColor = vec4(0.6*noise1(5), 0.3, 0.4, 0.1);
	gl_Position = ftransform();
}