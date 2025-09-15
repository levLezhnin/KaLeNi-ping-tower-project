export default function Footer() {
  return (
    <footer className="bg-gray-900 text-white py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <div className="mb-4 flex justify-center items-center gap-3">
          <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-blue-500 rounded-lg flex items-center justify-center">
            <span className="text-white font-bold text-sm">M</span>
          </div>
          <span className="font-semibold">MonitorPro</span>
        </div>
        <p className="text-gray-400 text-sm">&copy; 2025 MonitorPro</p>
      </div>
    </footer>
  );
}